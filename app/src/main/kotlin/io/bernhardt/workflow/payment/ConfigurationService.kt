package io.bernhardt.workflow.payment

import com.fasterxml.jackson.annotation.JsonProperty

interface ConfigurationService {
    fun storeMerchantConfiguration(merchantId: MerchantId, config: MerchantConfiguration)
    fun storeUserConfiguration(userId: UserId, config: UserConfiguration)
    fun retrieveMerchantConfiguration(merchantId: MerchantId): MerchantConfiguration?
    fun retrieveUserConfiguration(userId: UserId): UserConfiguration?
}

data class MerchantConfiguration(
        @JsonProperty("merchantId") val merchantId: MerchantId,
        @JsonProperty("bankIdentifier") val bankIdentifier: BankIdentifier
)

data class UserConfiguration(
        @JsonProperty("paymentMethod") val paymentMethod: PaymentMethod
)