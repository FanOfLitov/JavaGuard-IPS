export default function StatList({
                                     title,
                                     values
                                 }) {

    const entries =
        Object.entries(
            values ?? {}
        )


    return (
        <section className="panel">

            <h2>
                {title}
            </h2>


            <div className="stat-list">

                {
                    entries.length === 0
                        ? (
                            <div className="stat-row">
                                <span>No data</span>
                            </div>
                        )
                        : entries.map(
                            ([name, value]) => (

                                <div
                                    className="stat-row"
                                    key={name}
                                >

                                    <span>
                                        {name}
                                    </span>

                                    <strong>
                                        {value}
                                    </strong>

                                </div>
                            )
                        )
                }

            </div>

        </section>
    )
}