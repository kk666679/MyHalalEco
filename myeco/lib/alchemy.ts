import { Alchemy, Network } from "alchemy-sdk"

export class AlchemyService {
  private static instance: Alchemy

  static getInstance(): Alchemy {
    if (!this.instance) {
      const config = {
        apiKey: process.env.ALCHEMY_API_KEY!,
        network: Network.ETH_MAINNET,
      }
      this.instance = new Alchemy(config)
    }
    return this.instance
  }

  static async getBlockNumber(): Promise<number> {
    const alchemy = this.getInstance()
    return await alchemy.core.getBlockNumber()
  }

  static async getBalance(address: string): Promise<string> {
    const alchemy = this.getInstance()
    const balance = await alchemy.core.getBalance(address)
    return balance.toString()
  }

  static async getTransactionReceipt(txHash: string) {
    const alchemy = this.getInstance()
    return await alchemy.core.getTransactionReceipt(txHash)
  }

  static async verifyHalalCertification(certificationId: string): Promise<{
    isValid: boolean
    blockchainRecord?: any
    certificationData?: any
  }> {
    try {
      // Simulate blockchain verification
      // In a real implementation, this would query your smart contract
      const alchemy = this.getInstance()
      const blockNumber = await alchemy.core.getBlockNumber()

      // Mock verification logic
      const isValid = certificationId.startsWith("JAKIM") || certificationId.startsWith("HALAL")

      return {
        isValid,
        blockchainRecord: {
          blockNumber,
          timestamp: Date.now(),
          verificationHash: `0x${Buffer.from(certificationId).toString("hex")}`,
        },
        certificationData: {
          id: certificationId,
          authority: isValid ? "JAKIM Malaysia" : "Unknown",
          status: isValid ? "Active" : "Invalid",
          expiryDate: new Date(Date.now() + 365 * 24 * 60 * 60 * 1000).toISOString(),
        },
      }
    } catch (error) {
      console.error("Blockchain verification failed:", error)
      return { isValid: false }
    }
  }

  static async createHalalCertificationRecord(certificationData: {
    productId: string
    certificationId: string
    authority: string
    expiryDate: string
  }): Promise<{ success: boolean; transactionHash?: string; error?: string }> {
    try {
      // In a real implementation, this would interact with your smart contract
      // For now, we'll simulate the transaction
      const mockTxHash = `0x${Buffer.from(JSON.stringify(certificationData)).toString("hex").slice(0, 64)}`

      return {
        success: true,
        transactionHash: mockTxHash,
      }
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : "Unknown error",
      }
    }
  }
}
