import { type NextRequest, NextResponse } from "next/server"
import { AuthService } from "@/lib/auth"
import { HalalValidatorService } from "@/lib/halal-validator"

export async function POST(request: NextRequest) {
  try {
    // Authenticate request
    const user = await AuthService.authenticateRequest(request)
    if (!user) {
      return NextResponse.json({ success: false, message: "Authentication required" }, { status: 401 })
    }

    const validationRequest = await request.json()

    // Validate required fields
    if (!validationRequest.product || !validationRequest.ingredients) {
      return NextResponse.json(
        { success: false, message: "Product name and ingredients are required" },
        { status: 400 },
      )
    }

    // Perform Halal validation
    const result = await HalalValidatorService.validateProduct(validationRequest)

    return NextResponse.json({
      success: true,
      data: result,
    })
  } catch (error) {
    console.error("Validation error:", error)
    return NextResponse.json({ success: false, message: "Validation service error" }, { status: 500 })
  }
}
