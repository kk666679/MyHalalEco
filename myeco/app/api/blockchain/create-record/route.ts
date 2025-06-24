import { type NextRequest, NextResponse } from "next/server"
import { AuthService } from "@/lib/auth"
import { AlchemyService } from "@/lib/alchemy"

export async function POST(request: NextRequest) {
  try {
    // Authenticate request
    const user = await AuthService.authenticateRequest(request)
    if (!user) {
      return NextResponse.json({ success: false, message: "Authentication required" }, { status: 401 })
    }

    const certificationData = await request.json()

    // Validate required fields
    const requiredFields = ["productId", "certificationId", "authority", "expiryDate"]
    for (const field of requiredFields) {
      if (!certificationData[field]) {
        return NextResponse.json({ success: false, message: `${field} is required` }, { status: 400 })
      }
    }

    // Create blockchain record
    const result = await AlchemyService.createHalalCertificationRecord(certificationData)

    return NextResponse.json({
      success: result.success,
      data: result.success ? { transactionHash: result.transactionHash } : null,
      message: result.success ? "Certification record created" : result.error,
    })
  } catch (error) {
    console.error("Blockchain record creation error:", error)
    return NextResponse.json({ success: false, message: "Failed to create blockchain record" }, { status: 500 })
  }
}
