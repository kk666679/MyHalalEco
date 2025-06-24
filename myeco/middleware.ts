import { NextResponse } from "next/server"
import type { NextRequest } from "next/server"

export async function middleware(request: NextRequest) {
  // Protected routes
  const protectedPaths = ["/dashboard"]
  const protectedApiPaths = ["/api/validate-halal", "/api/blockchain"]

  const isProtectedPath = protectedPaths.some((path) => request.nextUrl.pathname.startsWith(path))
  const isProtectedApiPath = protectedApiPaths.some((path) => request.nextUrl.pathname.startsWith(path))

  if (isProtectedPath || isProtectedApiPath) {
    // Check for auth token in cookies
    const token = request.cookies.get("auth-token")

    if (!token) {
      // Redirect to login for dashboard routes
      if (isProtectedPath) {
        return NextResponse.redirect(new URL("/login", request.url))
      }

      // Return 401 for API routes
      if (isProtectedApiPath) {
        return NextResponse.json({ success: false, message: "Authentication required" }, { status: 401 })
      }
    }
  }

  return NextResponse.next()
}

export const config = {
  matcher: ["/dashboard/:path*", "/api/validate-halal/:path*", "/api/blockchain/:path*"],
}
