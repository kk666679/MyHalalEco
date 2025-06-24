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

    const { certificationId } = await request.json()

    if (!certificationId) {
      return NextResponse.json({ success: false, message: "Certification ID is required" }, { status: 400 })
    }

    // Verify certification on blockchain
    const verification = await AlchemyService.verifyHalalCertification(certificationId)

    return NextResponse.json({
      success: true,
      data: verification,
    })
  } catch (error) {
    console.error("Blockchain verification error:", error)
    return NextResponse.json({ success: false, message: "Blockchain verification failed" }, { status: 500 })
  }
}
