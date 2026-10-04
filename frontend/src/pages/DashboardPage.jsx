import {
    useCallback,
    useEffect,
    useState
} from 'react'

import {
    getDashboardSummary,
    startCapture,
    stopCapture
} from '../api/javaGuardApi'

import StatCard from '../components/StatCard'
import StatList from '../components/StatList'
import CapturePanel from '../components/CapturePanel'
import AlertsTable from '../components/AlertsTable'


export default function DashboardPage() {

    const [dashboard, setDashboard] =
        useState(null)

    const [error, setError] =
        useState(null)

    const [connected, setConnected] =
        useState(false)

    const [loading, setLoading] =
        useState(true)


    const loadDashboard =
        useCallback(
            async () => {

                try {

                    const data =
                        await getDashboardSummary()

                    setDashboard(data)

                    setConnected(true)

                    setError(null)

                } catch (error) {

                    setConnected(false)

                    setError(
                        error.message
                    )

                } finally {

                    setLoading(false)
                }
            },
            []
        )


    useEffect(
        () => {

            loadDashboard()


            const timer =
                setInterval(
                    loadDashboard,
                    2000
                )


            return () =>
                clearInterval(timer)

        },
        [loadDashboard]
    )


    async function handleStart(
        interfaceName
    ) {

        try {

            await startCapture(
                interfaceName
            )

            await loadDashboard()

        } catch (error) {

            setError(
                error.message
            )
        }
    }


    async function handleStop() {

        try {

            await stopCapture()

            await loadDashboard()

        } catch (error) {

            setError(
                error.message
            )
        }
    }


    if (
        loading
        &&
        !dashboard
    ) {

        return (
            <div className="loading-screen">
                Loading JavaGuard...
            </div>
        )
    }


    const capture =
        dashboard?.capture ?? {}

    const traffic =
        dashboard?.traffic ?? {}

    const security =
        dashboard?.security ?? {}


    return (
        <>

            <header className="header">

                <div>

                    <h1>
                        JavaGuard IPS
                    </h1>

                    <p>
                        Network Security Dashboard
                    </p>

                </div>


                <div
                    className={
                        connected
                            ? 'connection connected'
                            : 'connection disconnected'
                    }
                >
                    {
                        connected
                            ? 'Connected'
                            : 'Disconnected'
                    }
                </div>

            </header>


            <main className="container">

                {
                    error
                    &&
                    (
                        <div className="error-message">
                            {error}
                        </div>
                    )
                }


                <CapturePanel
                    capture={capture}
                    onStart={handleStart}
                    onStop={handleStop}
                />


                <section className="cards">

                    <StatCard
                        title="Capture"
                        value={
                            capture.running
                                ? 'RUNNING'
                                : 'STOPPED'
                        }
                    />

                    <StatCard
                        title="Processed Events"
                        value={
                            capture.processedEvents
                            ?? 0
                        }
                    />

                    <StatCard
                        title="Dropped Events"
                        value={
                            capture.droppedEvents
                            ?? 0
                        }
                    />

                    <StatCard
                        title="Packets / second"
                        value={
                            traffic.packetsPerSecond
                            ?? 0
                        }
                    />

                    <StatCard
                        title="Total Alerts"
                        value={
                            security.totalAlerts
                            ?? 0
                        }
                    />

                    <StatCard
                        title="Alerts 24h"
                        value={
                            security.alertsLast24Hours
                            ?? 0
                        }
                    />

                </section>


                <section className="dashboard-grid">

                    <StatList
                        title="Protocols"
                        values={
                            traffic.protocolCounts
                        }
                    />

                    <StatList
                        title="Threat Types"
                        values={
                            security.byThreatType
                        }
                    />

                    <StatList
                        title="Severity"
                        values={
                            security.bySeverity
                        }
                    />


                    <section className="panel">

                        <h2>
                            Top Source IPs
                        </h2>


                        <div className="stat-list">

                            {
                                !security.topSourceIps
                                ||
                                security.topSourceIps.length === 0
                                    ? (
                                        <div className="stat-row">
                                            No data
                                        </div>
                                    )
                                    : security.topSourceIps.map(
                                        item => (

                                            <div
                                                className="stat-row"
                                                key={item.sourceIp}
                                            >

                                                <span>
                                                    {item.sourceIp}
                                                </span>

                                                <strong>
                                                    {item.count}
                                                </strong>

                                            </div>
                                        )
                                    )
                            }

                        </div>

                    </section>

                </section>


                <AlertsTable
                    alerts={
                        dashboard?.recentAlerts
                        ?? []
                    }
                />

            </main>

        </>
    )
}