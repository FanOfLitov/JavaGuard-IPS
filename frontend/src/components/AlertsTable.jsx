function formatTimestamp(timestamp) {

    if (!timestamp) {
        return '-'
    }

    return new Date(
        timestamp
    ).toLocaleString()
}


export default function AlertsTable({
                                        alerts
                                    }) {

    return (
        <section className="panel alerts-panel">

            <div className="section-heading">

                <div>

                    <h2>
                        Recent Security Alerts
                    </h2>

                    <p>
                        Security events stored in PostgreSQL
                    </p>

                </div>

            </div>


            <div className="table-wrapper">

                <table>

                    <thead>

                    <tr>
                        <th>Time</th>
                        <th>Threat</th>
                        <th>Severity</th>
                        <th>Source</th>
                        <th>Destination</th>
                        <th>Evidence</th>
                        <th>Description</th>
                    </tr>

                    </thead>


                    <tbody>

                    {
                        !alerts
                        ||
                        alerts.length === 0
                            ? (
                                <tr>

                                    <td colSpan="7">
                                        No security alerts
                                    </td>

                                </tr>
                            )
                            : alerts.map(
                                alert => (

                                    <tr key={alert.id}>

                                        <td>
                                            {
                                                formatTimestamp(
                                                    alert.timestamp
                                                )
                                            }
                                        </td>

                                        <td>
                                            {alert.type}
                                        </td>

                                        <td>

                                                <span
                                                    className={
                                                        `severity ${alert.severity}`
                                                    }
                                                >
                                                    {alert.severity}
                                                </span>

                                        </td>

                                        <td>
                                            {alert.sourceIp}
                                        </td>

                                        <td>
                                            {alert.destinationIp}
                                        </td>

                                        <td>
                                            {alert.evidenceCount}
                                        </td>

                                        <td>
                                            {alert.description}
                                        </td>

                                    </tr>
                                )
                            )
                    }

                    </tbody>

                </table>

            </div>

        </section>
    )
}