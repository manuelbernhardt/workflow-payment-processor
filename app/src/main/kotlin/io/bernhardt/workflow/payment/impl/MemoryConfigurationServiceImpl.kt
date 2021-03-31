package io.bernhardt.workflow.payment.impl

import io.bernhardt.workflow.payment.*

/**
 * Simple, in-memory implementation of a Configuration Service
 */
class MemoryConfigurationServiceImpl : ConfigurationService {

    private val merchantConfigurations = mutableMapOf<MerchantId, MerchantConfiguration>()
    private val userConfigurations = mutableMapOf<UserId, UserConfiguration>()

    override fun storeMerchantConfiguration(merchantId: MerchantId, config: MerchantConfiguration) {
        merchantConfigurations[merchantId] = config
    }

    override fun storeUserConfiguration(userId: UserId, config: UserConfiguration) {
        userConfigurations[userId] = config
    }

    override fun retrieveMerchantConfiguration(merchantId: MerchantId): MerchantConfiguration? {
        return merchantConfigurations[merchantId]
    }

    override fun retrieveUserConfiguration(userId: UserId): UserConfiguration? {
        return userConfigurations[userId]
    }
}