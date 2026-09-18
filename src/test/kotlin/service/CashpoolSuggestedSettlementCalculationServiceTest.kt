package service

import core.exceptions.CashpoolNotFound
import domain.commands.CreateCashpoolCommand
import domain.commands.CreateCashpoolMemberCommand
import domain.commands.CreateCashpoolTransactionCommand
import domain.contexts.ServiceContext
import domain.repositories.*
import kotlinx.coroutines.runBlocking
import org.junit.Test
import service.cashpool.CashpoolService
import service.cashpool_member.CashpoolMemberService
import service.cashpool_settlement.CashpoolSettlementService
import service.cashpool_settlement.CashpoolSuggestedSettlementCalculationService
import service.cashpool_settlement.CashpoolSuggestedSettlementCalculationMember
import service.cashpool_transaction.CashpoolTransactionService
import service.user.UserService
import testutils.Commands
import testutils.Contexts
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class CashpoolSuggestedSettlementCalculationServiceTest : BaseServiceTest() {
    private val userRepo = UserRepositoryImpl()
    private val userService = UserService(userRepo)
    private val cashpoolRepo = CashpoolRepositoryImpl()
    private val cashpoolSettlementRepo = CashpoolSettlementRepositoryImpl()
    private val cashpoolService = CashpoolService(userService, cashpoolRepo)
    private val cashpoolSettlementService = CashpoolSettlementService(cashpoolSettlementRepo, cashpoolService)
    private val cashpoolMemberRepo = CashpoolMemberRepositoryImpl()
    private val cashpoolMemberService = CashpoolMemberService(cashpoolMemberRepo, userRepo, cashpoolRepo)
    private val transactionRepo = CashpoolTransactionRepositoryImpl()
    private val transactionService = CashpoolTransactionService(transactionRepo, cashpoolService, userService)
    private val settlementService =
        CashpoolSuggestedSettlementCalculationService(cashpoolService, cashpoolSettlementService, transactionService, cashpoolMemberService, userService)

    @Test
    fun `calculateSettlements - success`() {
        runBlocking {
            val userA = context(Contexts.internal) { userService.create(Commands.User.create(firstName = "Sarah")) }
            val userB = context(Contexts.internal) { userService.create(Commands.User.create(firstName = "Elias")) }
            val userC = context(Contexts.internal) { userService.create(Commands.User.create(firstName = "Leon")) }
            val userD = context(Contexts.internal) { userService.create(Commands.User.create(firstName = "Donald")) }
            val userE = context(Contexts.internal) { userService.create(Commands.User.create(firstName = "Mina")) }
            val users = listOf(userA, userB, userC, userD, userE)
            val cpId = context(Contexts.of(userA)) { cashpoolService.create(CreateCashpoolCommand("T", "D", userA.id)).id }

            users.forEach { cashpoolMemberRepo.create(CreateCashpoolMemberCommand(it.id, cpId)) }

            createTransaction(cpId, userA.id, -50_00)
            createTransaction(cpId, userB.id, -80_41)
            createTransaction(cpId, userC.id, -93_65)
            createTransaction(cpId, userD.id, -20_00)
            createTransaction(cpId, userE.id, 0)

            val result = context(Contexts.of(userA)) { settlementService.calculateSettlements(cpId) }

            assertEquals(4, result.size)

            // mina sends leon 4484€
            assertEquals(userE.id, result[0].from.id)
            assertEquals(userC.id, result[0].to.id)
            assertEquals(44_84, result[0].amountCents)
        }
    }

    @Test
    fun `calculateSettlements - not found - fails`() {
        runBlocking {
            val user = context(Contexts.internal) { userService.create(Commands.User.create()) }
            assertFailsWith<CashpoolNotFound> {
                context(Contexts.of(user)) {
                    settlementService.calculateSettlements(999)
                }
            }
        }
    }

    @Test
    fun `calculateSettlements - empty members - returns empty list`() {
        runBlocking {
            val owner = context(Contexts.internal) { userService.create(Commands.User.create()) }
            val cpId = context(Contexts.of(owner)) { cashpoolService.create(CreateCashpoolCommand("T", "D", owner.id)).id }
            cashpoolMemberRepo.create(CreateCashpoolMemberCommand(owner.id, cpId))
            // No OTHER members added

            val result = context(Contexts.of(owner)) { settlementService.calculateSettlements(cpId) }
            assertEquals(0, result.size)
        }
    }

    @Test
    fun `calculateSettlements - more credit than debt scenario`() {
        runBlocking {
            val u1 = context(Contexts.internal) { userService.create(Commands.User.create(firstName = "U1", email = "u1@ex.com")) }
            val u2 = context(Contexts.internal) { userService.create(Commands.User.create(firstName = "U2", email = "u2@ex.com")) }
            val u3 = context(Contexts.internal) { userService.create(Commands.User.create(firstName = "U3", email = "u3@ex.com")) }
            val cpId = context(Contexts.of(u1)) { cashpoolService.create(CreateCashpoolCommand("T", "D", u1.id)).id }

            listOf(u1.id, u2.id, u3.id).forEach { cashpoolMemberRepo.create(CreateCashpoolMemberCommand(it, cpId)) }

            createTransaction(cpId, u1.id, 20_00) // Creditor (+10.00)
            createTransaction(cpId, u2.id, 10_01) // Creditor (+0.01)
            createTransaction(cpId, u3.id, 0)     // Debtor (-10.00 approx)

            val result = context(Contexts.of(u1)) { settlementService.calculateSettlements(cpId) }

            assertEquals(1, result.size)
        }
    }

    @Test
    fun `calculateSettlements - with excluded users`() {
        runBlocking {
            val userA = context(Contexts.internal) { userService.create(Commands.User.create(firstName = "Sarah")) }
            val userB = context(Contexts.internal) { userService.create(Commands.User.create(firstName = "Elias")) }
            val userC = context(Contexts.internal) { userService.create(Commands.User.create(firstName = "Leon")) }
            val cpId = context(Contexts.of(userA)) { cashpoolService.create(CreateCashpoolCommand("T", "D", userA.id)).id }

            listOf(userA.id, userB.id, userC.id).forEach { cashpoolMemberRepo.create(CreateCashpoolMemberCommand(it, cpId)) }

            createTransaction(cpId, userA.id, -60_00, excludedUsers = listOf(userC.id))
            createTransaction(cpId, userB.id, 60_00, excludedUsers = listOf(userA.id))

            val result = context(Contexts.of(userA)) { settlementService.calculateSettlements(cpId) }
            assertNotNull(result)
        }
    }

    @Test
    fun `calculateSettlements - excluded user not in settlement members - safely skips`() {
        runBlocking {
            val userA = context(Contexts.internal) { userService.create(Commands.User.create(firstName = "Sarah")) }
            val userB = context(Contexts.internal) { userService.create(Commands.User.create(firstName = "Elias")) }
            val cpId = context(Contexts.of(userA)) { cashpoolService.create(CreateCashpoolCommand("T", "D", userA.id)).id }

            listOf(userA.id, userB.id).forEach { cashpoolMemberRepo.create(CreateCashpoolMemberCommand(it, cpId)) }

            createTransaction(cpId, userA.id, -50_00, excludedUsers = listOf(9999))

            val result = context(Contexts.of(userA)) { settlementService.calculateSettlements(cpId) }
            assertEquals(1, result.size)
            assertEquals(userB.id, result[0].from.id)
            assertEquals(userA.id, result[0].to.id)
            assertEquals(25_00, result[0].amountCents)
        }
    }

    @Test
    fun `CashpoolSuggestedSettlementCalculationMember toString formatting`() {
        runBlocking {
            val user = context(Contexts.internal) { userService.create(Commands.User.create(firstName = "John", lastName = "Doe")) }
            val cpId = context(Contexts.of(user)) { cashpoolService.create(CreateCashpoolCommand("T", "D", user.id)).id }
            val member = cashpoolMemberRepo.create(CreateCashpoolMemberCommand(user.id, cpId))
            val calcMember = CashpoolSuggestedSettlementCalculationMember(
                member = member,
                balancePaid = java.math.BigDecimal("50.00"),
                totalExcluded = java.math.BigDecimal("10.00")
            )
            assertEquals(
                "CashpoolSuggestedSettlementCalculationMember(\"John Doe\", 50.00 €, excluded = 10.00 €)",
                calcMember.toString()
            )
        }
    }

    private suspend fun createTransaction(
        cashpoolId: Int,
        userId: Int,
        amountCents: Long,
        excludedUsers: List<Int> = emptyList()
    ) {
        context(Contexts.of(userId)) {
            transactionService.create(
                CreateCashpoolTransactionCommand(
                    userId,
                    cashpoolId,
                    "T1",
                    amountCents,
                    excludedUsers
                )
            )
        }
    }
}
