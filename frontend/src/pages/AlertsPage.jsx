import {
    useEffect,
    useState
} from 'react'

import {
    useSearchParams
} from 'react-router'

import {
    searchAlerts
} from '../api/javaGuardApi'


export default function AlertsPage() {

    const [
        searchParams,
        setSearchParams
    ] = useSearchParams()


    const [result, setResult] =
        useState(null)

    const [loading, setLoading] =
        useState(true)

    const [error, setError] =
        useState(null)


    const type =
        searchParams.get('type')
        ?? ''

    const severity =
        searchParams.get('severity')
        ?? ''

    const sourceIp =
        searchParams.get('sourceIp')
        ?? ''

    const destinationIp =
        searchParams.get('destinationIp')
        ?? ''

    const page =
        Number(
            searchParams.get('page')
            ?? 0
        )

    const size =
        Number(
            searchParams.get('size')
            ?? 20
        )


    useEffect(
        () => {

            async function loadAlerts() {

                try {

                    setLoading(true)

                    const data =
                        await searchAlerts({
                            type,
                            severity,
                            sourceIp,
                            destinationIp,
                            page,
                            size
                        })

                    setResult(data)

                    setError(null)

                } catch (error) {

                    setError(
                        error.message
                    )

                } finally {

                    setLoading(false)
                }
            }


            loadAlerts()

        },
        [
            type,
            severity,
            sourceIp,
            destinationIp,
            page,
            size
        ]
    )


    function updateFilter(
        name,
        value
    ) {

        const next =
            new URLSearchParams(
                searchParams
            )


        if (value) {

            next.set(
                name,
                value
            )

        } else {

            next.delete(name)
        }


        /*
         * При изменении фильтра всегда
         * возвращаемся на первую страницу.
         */
        next.set(
            'page',
            '0'
        )


        setSearchParams(next)
    }


    function changePage(
        newPage
    ) {

        if (newPage < 0) {
            return
        }


        const next =
            new URLSearchParams(
                searchParams
            )


        next.set(
            'page',
            String(newPage)
        )


        setSearchParams(next)
    }


    function resetFilters() {

        setSearchParams({
            page: '0',
            size: String(size)
        })
    }


    function formatTimestamp(
        timestamp
    ) {

        if (!timestamp) {
            return '-'
        }


        return new Date(
            timestamp
        ).toLocaleString()
    }


    return (
        <>

            <section className="page-heading">

                <h2>
                    Security Alerts
                </h2>

                <p>
                    Search and investigate detected security events
                </p>

            </section>


            <section className="panel filters-panel">

                <div className="filters-grid">


                    <label>

                        Threat Type

                        <select
                            value={type}
                            onChange={
                                event =>
                                    updateFilter(
                                        'type',
                                        event.target.value
                                    )
                            }
                        >

                            <option value="">
                                All
                            </option>

                            <option value="PORT_SCAN">
                                Port Scan
                            </option>

                            <option value="SYN_FLOOD">
                                SYN Flood
                            </option>

                        </select>

                    </label>


                    <label>

                        Severity

                        <select
                            value={severity}
                            onChange={
                                event =>
                                    updateFilter(
                                        'severity',
                                        event.target.value
                                    )
                            }
                        >

                            <option value="">
                                All
                            </option>

                            <option value="LOW">
                                Low
                            </option>

                            <option value="MEDIUM">
                                Medium
                            </option>

                            <option value="HIGH">
                                High
                            </option>

                            <option value="CRITICAL">
                                Critical
                            </option>

                        </select>

                    </label>


                    <label>

                        Source IP

                        <input
                            type="text"
                            value={sourceIp}
                            placeholder="127.0.0.1"
                            onChange={
                                event =>
                                    updateFilter(
                                        'sourceIp',
                                        event.target.value
                                    )
                            }
                        />

                    </label>


                    <label>

                        Destination IP

                        <input
                            type="text"
                            value={destinationIp}
                            placeholder="10.0.0.10"
                            onChange={
                                event =>
                                    updateFilter(
                                        'destinationIp',
                                        event.target.value
                                    )
                            }
                        />

                    </label>

                </div>


                <button
                    type="button"
                    className="button secondary-button"
                    onClick={resetFilters}
                >
                    Reset filters
                </button>

            </section>


            {
                error
                &&
                (
                    <div className="error-message">
                        {error}
                    </div>
                )
            }


            <section className="panel">

                <div className="alerts-summary">

                    <span>
                        Total results:
                    </span>

                    <strong>
                        {
                            result?.totalElements
                            ?? 0
                        }
                    </strong>

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
                            loading
                                ? (
                                    <tr>

                                        <td colSpan="7">
                                            Loading...
                                        </td>

                                    </tr>
                                )

                                : !result?.content?.length
                                    ? (
                                        <tr>

                                            <td colSpan="7">
                                                No alerts found
                                            </td>

                                        </tr>
                                    )

                                    : result.content.map(
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


                <div className="pagination">


                    <button
                        type="button"
                        className="button secondary-button"
                        disabled={
                            loading
                            ||
                            (result?.first ?? true)
                        }
                        onClick={
                            () =>
                                changePage(
                                    page - 1
                                )
                        }
                    >
                        Previous
                    </button>


                    <span>

                        Page{' '}

                        <strong>
                            {
                                (result?.page ?? 0)
                                + 1
                            }
                        </strong>

                        {' '}of{' '}

                        <strong>
                            {
                                Math.max(
                                    result?.totalPages
                                    ?? 0,
                                    1
                                )
                            }
                        </strong>

                    </span>


                    <button
                        type="button"
                        className="button secondary-button"
                        disabled={
                            loading
                            ||
                            (result?.last ?? true)
                        }
                        onClick={
                            () =>
                                changePage(
                                    page + 1
                                )
                        }
                    >
                        Next
                    </button>


                </div>

            </section>

        </>
    )
}