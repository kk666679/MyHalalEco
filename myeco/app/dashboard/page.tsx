"use client"

import { useEffect, useState } from "react"
import { motion } from "framer-motion"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { useAuth } from "@/hooks/use-auth"
import { Shield, Activity, TrendingUp, Users, ArrowRight } from "lucide-react"
import { useRouter } from "next/navigation"

export default function DashboardPage() {
  const { user, isAuthenticated, loading } = useAuth()
  const router = useRouter()
  const [stats, setStats] = useState({
    validationsToday: 0,
    blockchainRecords: 0,
    activeUsers: 0,
    successRate: 0,
  })

  useEffect(() => {
    if (!loading && !isAuthenticated) {
      router.push("/login")
    }
  }, [isAuthenticated, loading, router])

  useEffect(() => {
    // Simulate loading dashboard stats
    const timer = setTimeout(() => {
      setStats({
        validationsToday: 1247,
        blockchainRecords: 8934,
        activeUsers: 2156,
        successRate: 99.8,
      })
    }, 1000)

    return () => clearTimeout(timer)
  }, [])

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-emerald-500"></div>
      </div>
    )
  }

  if (!isAuthenticated) {
    return null
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100 pt-16">
      <div className="container mx-auto px-4 py-8">
        {/* Header */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6 }}
          className="mb-8"
        >
          <h1 className="text-3xl font-bold text-gray-900 mb-2">Welcome back, {user?.name}!</h1>
          <p className="text-gray-600">Here's what's happening with your Halal ecosystem today.</p>
        </motion.div>

        {/* Stats Grid */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.2 }}
          className="grid md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8"
        >
          {[
            {
              title: "Validations Today",
              value: stats.validationsToday.toLocaleString(),
              icon: Shield,
              color: "text-emerald-600",
              bgColor: "bg-emerald-100",
            },
            {
              title: "Blockchain Records",
              value: stats.blockchainRecords.toLocaleString(),
              icon: Activity,
              color: "text-blue-600",
              bgColor: "bg-blue-100",
            },
            {
              title: "Active Users",
              value: stats.activeUsers.toLocaleString(),
              icon: Users,
              color: "text-purple-600",
              bgColor: "bg-purple-100",
            },
            {
              title: "Success Rate",
              value: `${stats.successRate}%`,
              icon: TrendingUp,
              color: "text-green-600",
              bgColor: "bg-green-100",
            },
          ].map((stat, index) => (
            <Card key={index}>
              <CardContent className="p-6">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm font-medium text-gray-600">{stat.title}</p>
                    <p className="text-2xl font-bold text-gray-900">{stat.value}</p>
                  </div>
                  <div className={`w-12 h-12 ${stat.bgColor} rounded-lg flex items-center justify-center`}>
                    <stat.icon className={`w-6 h-6 ${stat.color}`} />
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </motion.div>

        {/* Quick Actions */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.4 }}
          className="grid md:grid-cols-2 lg:grid-cols-3 gap-6"
        >
          {[
            {
              title: "Validate Product",
              description: "Check if a product meets Halal compliance standards",
              href: "/services/halal-validator",
              badge: "Popular",
            },
            {
              title: "Blockchain Verification",
              description: "Verify certification records on the blockchain",
              href: "/services/blockchain-verify",
              badge: "Secure",
            },
            {
              title: "Fraud Detection",
              description: "Analyze products for potential fraud indicators",
              href: "/services/fraud-detection",
              badge: "AI-Powered",
            },
            {
              title: "Supply Chain Tracking",
              description: "Track products through the entire supply chain",
              href: "/services/supply-chain",
              badge: "Beta",
            },
            {
              title: "Smart Contracts",
              description: "Generate Solidity contracts for Halal certification",
              href: "/services/smart-contracts",
              badge: "New",
            },
            {
              title: "API Documentation",
              description: "Integrate our services into your applications",
              href: "/docs/api",
              badge: "Developer",
            },
          ].map((action, index) => (
            <Card key={index} className="hover:shadow-lg transition-shadow cursor-pointer">
              <CardHeader>
                <div className="flex items-center justify-between">
                  <CardTitle className="text-lg">{action.title}</CardTitle>
                  <Badge variant="secondary">{action.badge}</Badge>
                </div>
                <CardDescription>{action.description}</CardDescription>
              </CardHeader>
              <CardContent>
                <Button variant="outline" className="w-full" asChild>
                  <a href={action.href}>
                    Get Started <ArrowRight className="ml-2 w-4 h-4" />
                  </a>
                </Button>
              </CardContent>
            </Card>
          ))}
        </motion.div>
      </div>
    </div>
  )
}
