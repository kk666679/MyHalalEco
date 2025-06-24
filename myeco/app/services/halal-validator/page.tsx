"use client"

import { motion } from "framer-motion"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Textarea } from "@/components/ui/textarea"
import { Label } from "@/components/ui/label"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Shield, CheckCircle, XCircle, AlertTriangle, ArrowRight, Code, Database, Zap } from "lucide-react"
import { useState } from "react"
import { useAuth } from "@/hooks/use-auth"

export default function HalalValidatorPage() {
  const [productData, setProductData] = useState(`{
  "product": "Beef Sausages",
  "ingredients": ["beef", "salt", "natural flavorings"],
  "certificationId": "JAKIM-2023-ABCD",
  "supplier": "Halal Foods Inc"
}`)
  const [result, setResult] = useState<any>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState("")

  const { isAuthenticated } = useAuth()

  const handleValidation = async () => {
    if (!isAuthenticated) {
      setError("Please sign in to use the validation service")
      return
    }

    setLoading(true)
    setError("")

    try {
      const parsedData = JSON.parse(productData)

      const response = await fetch("/api/validate-halal", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include",
        body: JSON.stringify(parsedData),
      })

      const data = await response.json()

      if (data.success) {
        setResult(data.data)
      } else {
        setError(data.message || "Validation failed")
      }
    } catch (err) {
      if (err instanceof SyntaxError) {
        setError("Invalid JSON format. Please check your input.")
      } else {
        setError("Validation failed. Please try again.")
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100 pt-16">
      {/* Hero Section */}
      <section className="py-20 bg-gradient-to-r from-emerald-600 to-teal-600 text-white">
        <div className="container mx-auto px-4">
          <motion.div
            initial={{ opacity: 0, y: 30 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8 }}
            className="text-center max-w-4xl mx-auto"
          >
            <div className="w-16 h-16 bg-white/20 rounded-full flex items-center justify-center mx-auto mb-6">
              <Shield className="w-8 h-8 text-white" />
            </div>
            <h1 className="text-4xl md:text-6xl font-bold mb-6">AI-Powered Halal Certification Validator</h1>
            <p className="text-xl md:text-2xl text-white/90 max-w-3xl mx-auto">
              Automated Halal verification for e-commerce listings with blockchain integration and comprehensive
              compliance analysis
            </p>
          </motion.div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-16 bg-white">
        <div className="container mx-auto px-4">
          <div className="grid md:grid-cols-3 gap-8">
            {[
              {
                icon: Zap,
                title: "Instant Analysis",
                description: "Get real-time Halal compliance results in seconds",
              },
              {
                icon: Database,
                title: "Blockchain Verified",
                description: "All certifications are stored on immutable blockchain",
              },
              {
                icon: Code,
                title: "API Integration",
                description: "Easy integration with your existing e-commerce platform",
              },
            ].map((feature, index) => (
              <motion.div
                key={index}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.2, duration: 0.6 }}
                className="text-center"
              >
                <div className="w-12 h-12 bg-emerald-100 rounded-lg flex items-center justify-center mx-auto mb-4">
                  <feature.icon className="w-6 h-6 text-emerald-600" />
                </div>
                <h3 className="text-xl font-semibold mb-2">{feature.title}</h3>
                <p className="text-gray-600">{feature.description}</p>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* Demo Section */}
      <section className="py-20">
        <div className="container mx-auto px-4">
          <div className="grid lg:grid-cols-2 gap-12">
            {/* Input Form */}
            <motion.div initial={{ opacity: 0, x: -50 }} animate={{ opacity: 1, x: 0 }} transition={{ duration: 0.8 }}>
              <Card className="shadow-lg">
                <CardHeader>
                  <CardTitle>Try the Validator</CardTitle>
                  <CardDescription>Enter product information to test our AI-powered Halal validation</CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                  {error && (
                    <Alert variant="destructive">
                      <AlertDescription>{error}</AlertDescription>
                    </Alert>
                  )}

                  <div>
                    <Label htmlFor="product-data">Product Data (JSON)</Label>
                    <Textarea
                      id="product-data"
                      value={productData}
                      onChange={(e) => setProductData(e.target.value)}
                      className="min-h-[200px] font-mono text-sm"
                    />
                  </div>
                  <Button onClick={handleValidation} className="w-full" disabled={loading}>
                    {loading ? "Validating..." : "Validate Product"}
                    <ArrowRight className="ml-2 w-4 h-4" />
                  </Button>

                  {!isAuthenticated && (
                    <p className="text-sm text-gray-600 text-center">
                      <a href="/login" className="text-emerald-600 hover:underline">
                        Sign in
                      </a>{" "}
                      to use the validation service
                    </p>
                  )}
                </CardContent>
              </Card>
            </motion.div>

            {/* Results */}
            <motion.div initial={{ opacity: 0, x: 50 }} animate={{ opacity: 1, x: 0 }} transition={{ duration: 0.8 }}>
              <Card className="shadow-lg">
                <CardHeader>
                  <CardTitle>Validation Results</CardTitle>
                  <CardDescription>AI analysis results with compliance status</CardDescription>
                </CardHeader>
                <CardContent>
                  {result ? (
                    <div className="space-y-4">
                      <div className="flex items-center gap-3">
                        {result.isHalalCompliant ? (
                          <CheckCircle className="w-6 h-6 text-green-500" />
                        ) : (
                          <XCircle className="w-6 h-6 text-red-500" />
                        )}
                        <div>
                          <div className="font-semibold">
                            {result.isHalalCompliant ? "Halal Compliant" : "Not Halal Compliant"}
                          </div>
                          <div className="text-sm text-gray-600">Confidence: {result.confidenceScore}%</div>
                        </div>
                      </div>

                      <div className="space-y-2">
                        <div className="text-sm">
                          <strong>Certification Authority:</strong> {result.certificationAuthority}
                        </div>
                        {result.blockchainVerificationLink && (
                          <div className="text-sm">
                            <strong>Blockchain Verification:</strong>
                            <a
                              href={result.blockchainVerificationLink}
                              className="text-emerald-600 hover:underline ml-1"
                            >
                              View on Blockchain
                            </a>
                          </div>
                        )}
                      </div>

                      {result.haramIngredients && result.haramIngredients.length > 0 && (
                        <div className="bg-red-50 p-3 rounded-lg">
                          <div className="flex items-center gap-2 text-red-700 font-medium mb-2">
                            <AlertTriangle className="w-4 h-4" />
                            Haram Ingredients Detected
                          </div>
                          <ul className="text-sm text-red-600">
                            {result.haramIngredients.map((ingredient: string, index: number) => (
                              <li key={index}>• {ingredient}</li>
                            ))}
                          </ul>
                        </div>
                      )}

                      {result.riskScore && (
                        <div className="bg-yellow-50 p-3 rounded-lg">
                          <div className="text-sm">
                            <strong>Risk Score:</strong> {result.riskScore}/10
                          </div>
                          {result.redFlags && result.redFlags.length > 0 && (
                            <div className="mt-2">
                              <strong className="text-sm">Red Flags:</strong>
                              <ul className="text-sm text-yellow-700 mt-1">
                                {result.redFlags.map((flag: string, index: number) => (
                                  <li key={index}>• {flag}</li>
                                ))}
                              </ul>
                            </div>
                          )}
                        </div>
                      )}
                    </div>
                  ) : (
                    <div className="text-center py-8 text-gray-500">
                      Enter product data and click validate to see results
                    </div>
                  )}
                </CardContent>
              </Card>
            </motion.div>
          </div>
        </div>
      </section>

      {/* API Documentation */}
      <section className="py-20 bg-gray-50">
        <div className="container mx-auto px-4">
          <motion.div
            initial={{ opacity: 0, y: 30 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8 }}
            viewport={{ once: true }}
            className="text-center mb-12"
          >
            <h2 className="text-3xl md:text-4xl font-bold mb-4">API Integration</h2>
            <p className="text-xl text-gray-600">
              Integrate our Halal validator into your applications with our simple REST API
            </p>
          </motion.div>

          <div className="max-w-4xl mx-auto">
            <Card>
              <CardHeader>
                <CardTitle>API Endpoint</CardTitle>
                <CardDescription>POST /api/validate-halal</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="bg-gray-900 text-green-400 p-4 rounded-lg font-mono text-sm overflow-x-auto">
                  <div className="mb-4">
                    <div className="text-gray-400">// Request</div>
                    <div>{`curl -X POST https://api.halaleco.com/validate-halal \\`}</div>
                    <div>{`  -H "Content-Type: application/json" \\`}</div>
                    <div>{`  -H "Authorization: Bearer YOUR_TOKEN" \\`}</div>
                    <div>{`  -d '{`}</div>
                    <div>{`    "product": "Beef Sausages",`}</div>
                    <div>{`    "ingredients": ["beef", "salt", "natural flavorings"],`}</div>
                    <div>{`    "certificationId": "JAKIM-2023-ABCD"`}</div>
                    <div>{`  }'`}</div>
                  </div>
                  <div>
                    <div className="text-gray-400">// Response</div>
                    <div>{`{`}</div>
                    <div>{`  "success": true,`}</div>
                    <div>{`  "data": {`}</div>
                    <div>{`    "isHalalCompliant": true,`}</div>
                    <div>{`    "haramIngredients": [],`}</div>
                    <div>{`    "certificationAuthority": "JAKIM Malaysia",`}</div>
                    <div>{`    "blockchainVerificationLink": "https://...",`}</div>
                    <div>{`    "confidenceScore": 98.5`}</div>
                    <div>{`  }`}</div>
                    <div>{`}`}</div>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </section>
    </div>
  )
}
