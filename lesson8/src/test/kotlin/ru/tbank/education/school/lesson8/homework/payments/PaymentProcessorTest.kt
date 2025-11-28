package ru.tbank.education.school.lesson8.homework.payments

import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertFalse

class PaymentProcessorTest {
    private lateinit var processor: PaymentProcessor

    @BeforeEach
    fun setUp() {
        processor = PaymentProcessor()
    }


    @Test
    @DisplayName("Некорректная сумма платежа: 0")
    fun processPaymentZeroAmount() {
        assertThrows(IllegalArgumentException::class.java) {
            processor.processPayment(
                amount = 0,
                cardNumber = "2200700948867071",
                expiryMonth = 12,
                expiryYear = 2025,
                currency = "RUB",
                customerId = "52678814142"
            )
        }
    }

    @Test
    @DisplayName("Некорректная сумма платежа: отрицательное значение")
    fun processPaymentNegativeAmount() {
        assertThrows(IllegalArgumentException::class.java) {
            processor.processPayment(
                amount = -1000,
                cardNumber = "2200700948867071",
                expiryMonth = 12,
                expiryYear = 2025,
                currency = "RUB",
                customerId = "52678814142"
            )
        }
    }

    @Test
    @DisplayName("Пустой номер карты")
    fun emptyCardNumber() {
        assertThrows(IllegalArgumentException::class.java) {
            processor.processPayment(
                5000,
                cardNumber = "",
                expiryMonth = 12,
                expiryYear = 2025,
                currency = "RUB",
                customerId = "52678814142"
            )
        }
    }

    @Test
    @DisplayName("Номер карты слишком длинный")
    fun cardNumberTooLong() {
        assertThrows(IllegalArgumentException::class.java) {
            processor.processPayment(
                5000,
                cardNumber = "2345678901234567899999992992939293",
                expiryMonth = 12,
                expiryYear = 2025,
                currency = "RUB",
                customerId = "52678814142"
            )
        }
    }

    @Test
    @DisplayName("Номер карты слишком короткий")
    fun cardNumberTooShort() {
        assertThrows(IllegalArgumentException::class.java) {
            processor.processPayment(
                5000,
                cardNumber = "123456789012",
                expiryMonth = 12,
                expiryYear = 2025,
                currency = "RUB",
                customerId = "52678814142"
            )
        }
    }

    @Test
    @DisplayName("Номер карты содержит не численные значения")
    fun cardNumberWithNonDigits() {
        assertThrows(IllegalArgumentException::class.java) {
            processor.processPayment(
                5000,
                cardNumber = "42424242HFH4242",
                expiryMonth = 12,
                expiryYear = 2025,
                currency = "RUB",
                customerId = "52678814142"
            )
        }
    }

    @Test
    @DisplayName("Просроченная дата карты")
    fun expiredCard() {
        assertThrows(IllegalArgumentException::class.java) {
            processor.processPayment(
                5000,
                cardNumber = "4242424242424242",
                expiryMonth = 10,
                expiryYear = 2024,
                currency = "RUB",
                customerId = "52678814142"
            )
        }
    }

    @Test
    @DisplayName("Неверный месяц истечения срока")
    fun invalidExpiryMonth() {
        assertThrows(IllegalArgumentException::class.java) {
            processor.processPayment(
                5000,
                cardNumber = "4242424242424242",
                expiryMonth = 13,
                expiryYear = 2025,
                currency = "RUB",
                customerId = "52678814142"
            )
        }
    }

    @Test
    @DisplayName("Пустой идентификатор клиента")
    fun emptyCustomerId() {
        assertThrows(IllegalArgumentException::class.java) {
            processor.processPayment(
                5000,
                cardNumber = "4242424242424242",
                expiryMonth = 12,
                expiryYear = 2025,
                currency = "RUB",
                customerId = ""
            )
        }
    }

    @Test
    @DisplayName("Пустая валюта")
    fun emptyCurrency() {
        assertThrows(IllegalArgumentException::class.java) {
            processor.processPayment(
                5000,
                cardNumber = "4242424242424242",
                expiryMonth = 12,
                expiryYear = 2025,
                currency = "",
                customerId = "12435456"
            )
        }
    }


    @Test
    @DisplayName("Подозрительные номера карт блокируются")
    fun suspiciousCardNumbers() {
        val suspiciousCards = listOf(
            "4444888812831234",
            "5555777773213333",
            "1111888899993444",
            "9999732737771256"
        )

        suspiciousCards.forEach { cardNumber ->
            val result = processor.processPayment(
                amount = 5000,
                cardNumber = cardNumber,
                expiryMonth = 12,
                expiryYear = 2025,
                currency = "RUB",
                customerId = "customer123"
            )
            assertEquals("REJECTED", result.status)
            assertEquals("Payment blocked due to suspected fraud", result.message)
        }
    }

    @Test
    @DisplayName("Карта с невалидным алгоритмом Луна блокируется")
    fun luhnInvalidCardIsSuspicious() {
        val result = processor.processPayment(
            amount = 100,
            cardNumber = "4242424242424241",
            expiryMonth = 12,
            expiryYear = 2025,
            currency = "RUB",
            customerId = "customer123"
        )

        assertEquals("REJECTED", result.status)
        assertEquals("Payment blocked due to suspected fraud", result.message)
    }

    @Test
    @DisplayName("Валидная карта по алгоритму Луна проходит проверку")
    fun luhnValidCardPassesCheck() {
        val result = processor.processPayment(
            amount = 100,
            cardNumber = "4242424242424242",
            expiryMonth = 12,
            expiryYear = 2025,
            currency = "RUB",
            customerId = "customer123"
        )

        assertTrue(result.status == "SUCCESS" || result.status == "FAILED")
        assertFalse(result.status == "REJECTED")
    }


    @Test
    @DisplayName("Конвертация валюты EUR")
    fun convertToEur() {
        val result = processor.processPayment(
            amount = 1000,
            cardNumber = "4111111111111111",
            expiryMonth = 12,
            expiryYear = 2025,
            currency = "EUR",
            customerId = "customer123"
        )

        assertEquals("SUCCESS", result.status)
    }

    @Test
    @DisplayName("Конвертация валюты GBP")
    fun convertToGbp() {
        val result = processor.processPayment(
            amount = 1000,
            cardNumber = "4111111111111111",
            expiryMonth = 12,
            expiryYear = 2025,
            currency = "GBP",
            customerId = "customer123"
        )

        assertEquals("SUCCESS", result.status)
    }

    @Test
    @DisplayName("Конвертация валюты USD")
    fun convertToUsd() {
        val result = processor.processPayment(
            amount = 1000,
            cardNumber = "4111111111111111",
            expiryMonth = 12,
            expiryYear = 2025,
            currency = "USD",
            customerId = "customer123"
        )

        assertEquals("SUCCESS", result.status)
    }

    @Test
    @DisplayName("Конвертация валюты JPY")
    fun convertToJpy() {
        val result = processor.processPayment(
            amount = 100,
            cardNumber = "4111111111111111",
            expiryMonth = 12,
            expiryYear = 2025,
            currency = "JPY",
            customerId = "customer123"
        )

        assertEquals("SUCCESS", result.status)
        assertEquals("Payment completed", result.message)
    }

    @Test
    @DisplayName("Конвертация валюты RUB")
    fun convertToRub() {
        val result = processor.processPayment(
            amount = 100,
            cardNumber = "4111111111111111",
            expiryMonth = 12,
            expiryYear = 2025,
            currency = "RUB",
            customerId = "customer123"
        )

        assertEquals("SUCCESS", result.status)
    }

    @Test
    @DisplayName("Неподдерживаемая валюта по умолчанию использует USD")
    fun unsupportedCurrencyDefaultsToUsd() {
        val result = processor.processPayment(
            amount = 100,
            cardNumber = "4242424242424242",
            expiryMonth = 12,
            expiryYear = 2026,
            currency = "KZT",
            customerId = "A1"
        )

        assertEquals("SUCCESS", result.status)
    }


    @Test
    @DisplayName("Карта с недостатком средств возвращает FAILED")
    fun insufficientFundsResult() {
        val result = processor.processPayment(
            amount = 50,
            cardNumber = "5500000012345678",
            expiryMonth = 12,
            expiryYear = 2026,
            currency = "USD",
            customerId = "A1"
        )

        assertEquals("FAILED", result.status)
        assertEquals("Insufficient funds", result.message)
    }



    @Test
    @DisplayName("Превышение лимита транзакции")
    fun transactionLimitExceeded() {
        val result = processor.processPayment(
            amount = 100001,
            cardNumber = "4111111111111111",
            expiryMonth = 12,
            expiryYear = 2026,
            currency = "USD",
            customerId = "1234567"
        )

        assertEquals("FAILED", result.status)
        assertTrue(result.message.contains("limit", ignoreCase = true))
    }

    @Test
    @DisplayName("Случайная ошибка шлюза при определенных суммах")
    fun gatewayRandomFailure() {
        val result = processor.processPayment(
            amount = 51,
            cardNumber = "4242424242424242",
            expiryMonth = 12,
            expiryYear = 2025,
            currency = "RUB",
            customerId = "customer123"
        )

        assertEquals("FAILED", result.status)
        assertTrue(result.message.contains("Gateway", ignoreCase = true) || result.message.isNotBlank())
    }

    @Test
    @DisplayName("Успешный платеж с нормальной картой")
    fun successfulPayment() {
        val result = processor.processPayment(
            amount = 1000,
            cardNumber = "4111111111111111",
            expiryMonth = 12,
            expiryYear = 2026,
            currency = "USD",
            customerId = "customer123"
        )

        assertEquals("SUCCESS", result.status)
        assertEquals("Payment completed", result.message)
    }



    @Test
    @DisplayName("Расчет скидки с отрицательной базовой суммой")
    fun loyaltyDiscountNegativeBaseAmount() {
        assertThrows(IllegalArgumentException::class.java) {
            processor.calculateLoyaltyDiscount(points = 6767, baseAmount = -67)
        }
    }

    @Test
    @DisplayName("Расчет скидки с минимальным количеством баллов")
    fun loyaltyDiscountMinimumPoints() {
        val discount = processor.calculateLoyaltyDiscount(points = 500, baseAmount = 7000)
        assertEquals(350, discount)
    }

    @Test
    @DisplayName("Расчет скидки с средним количеством баллов")
    fun loyaltyDiscountMediumPoints() {
        val discount = processor.calculateLoyaltyDiscount(points = 2000, baseAmount = 10000)
        assertEquals(1000, discount)
    }

    @Test
    @DisplayName("Расчет скидки с высоким количеством баллов")
    fun loyaltyDiscountHighPoints() {
        val discount = processor.calculateLoyaltyDiscount(points = 5000, baseAmount = 20000)
        assertEquals(3000, discount)
    }

    @Test
    @DisplayName("Максимальная скидка ограничена 5000")
    fun loyaltyDiscountMaximumLimit() {
        val discount = processor.calculateLoyaltyDiscount(points = 50000, baseAmount = 70000)
        assertEquals(5000, discount)
    }

    @Test
    @DisplayName("Нулевая скидка при недостаточных баллах")
    fun loyaltyDiscountZeroPoints() {
        val discount = processor.calculateLoyaltyDiscount(points = 100, baseAmount = 10000)
        assertEquals(0, discount)
    }

    @Test
    @DisplayName("Обработка успешных платежей")
    fun bulkProcessSuccessfulPayments() {
        val paymentList = listOf(
            PaymentData(100, "4242424242424242", 12, 2025, "RUB", "customer67"),
            PaymentData(200, "4242424242424242", 12, 2025, "RUB", "customer2")
        )
        val result = processor.bulkProcess(paymentList)
        assertEquals(paymentList.size, result.size)
        result.forEach { paymentResult ->
            assertTrue(paymentResult.status == "SUCCESS" || paymentResult.status == "FAILED")
        }
    }

    @Test
    @DisplayName("Обработка пустого списка платежей")
    fun bulkProcessEmptyList() {
        val paymentList = emptyList<PaymentData>()
        val result = processor.bulkProcess(paymentList)
        assertEquals(0, result.size)
    }



    @Test
    @DisplayName("Пакетная обработка с невалидными данными")
    fun bulkProcessWithInvalidData() {
        val payments = listOf(
            PaymentData(1000, "4111111111111111", 12, 2025, "USD", "customer1"),
            PaymentData(-100, "4111111111111111", 12, 2025, "USD", "customer2"),
            PaymentData(1000, "4111111111111111", 12, 2025, "", "customer3"),
            PaymentData(1000, "4111111111111111", 13, 2025, "USD", "customer4")
        )

        val results = processor.bulkProcess(payments)

        assertEquals(4, results.size)
        assertEquals("SUCCESS", results[0].status)
        assertEquals("REJECTED", results[1].status)
        assertTrue(results[1].message.contains("Amount must be positive"))
        assertEquals("REJECTED", results[2].status)
        assertTrue(results[2].message.contains("Currency cannot be empty"))
        assertEquals("REJECTED", results[3].status)
        assertTrue(results[3].message.contains("Invalid expiry date"))
    }

    @Test
    @DisplayName("Пакетная обработка с подозрительными картами")
    fun bulkProcessWithSuspiciousCards() {
        val payments = listOf(
            PaymentData(1000, "4444111122223333", 12, 2025, "USD", "customer1"),
            PaymentData(1000, "5555111122223333", 12, 2025, "USD", "customer2"),
            PaymentData(1000, "4111111111111111", 12, 2025, "USD", "customer3")
        )

        val results = processor.bulkProcess(payments)

        assertEquals(3, results.size)
        assertEquals("REJECTED", results[0].status)
        assertEquals("Payment blocked due to suspected fraud", results[0].message)
        assertEquals("REJECTED", results[1].status)
        assertEquals("Payment blocked due to suspected fraud", results[1].message)
        assertEquals("SUCCESS", results[2].status)
    }

    @Test
    @DisplayName("Пакетная обработка с различными статусами")
    fun bulkProcessMixedStatuses() {
        val payments = listOf(
            PaymentData(1000, "4111111111111111", 12, 2025, "USD", "customer1"),
            PaymentData(50, "5500000012345678", 12, 2025, "USD", "customer2"),
            PaymentData(1000, "4444888812345678", 12, 2025, "USD", "customer3"),
            PaymentData(100001, "4111111111111111", 12, 2025, "USD", "customer4")
        )

        val results = processor.bulkProcess(payments)

        assertEquals(4, results.size)
        assertEquals("SUCCESS", results[0].status)
        assertEquals("FAILED", results[1].status)
        assertEquals("Insufficient funds", results[1].message)
        assertEquals("REJECTED", results[2].status)
        assertEquals("Payment blocked due to suspected fraud", results[2].message)
        assertEquals("FAILED", results[3].status)
        assertTrue(results[3].message.contains("limit", ignoreCase = true))
    }


    @Test
    @DisplayName("Текущий месяц и год - валидная дата")
    fun currentMonthYearValid() {
        val result = processor.processPayment(
            amount = 1000,
            cardNumber = "4111111111111111",
            expiryMonth = 11,
            expiryYear = 2025,
            currency = "USD",
            customerId = "customer123"
        )

        assertTrue(result.status == "SUCCESS" || result.status == "FAILED")
    }

    @Test
    @DisplayName("Будущий год - валидная дата")
    fun futureYearValid() {
        val result = processor.processPayment(
            amount = 1000,
            cardNumber = "4111111111111111",
            expiryMonth = 5,
            expiryYear = 2026,
            currency = "USD",
            customerId = "customer123"
        )

        assertEquals("SUCCESS", result.status)
    }

    @Test
    @DisplayName("Прошлый год - невалидная дата")
    fun pastYearInvalid() {
        assertThrows(IllegalArgumentException::class.java) {
            processor.processPayment(
                amount = 1000,
                cardNumber = "4111111111111111",
                expiryMonth = 12,
                expiryYear = 2023,
                currency = "USD",
                customerId = "customer123"
            )
        }
    }
}
