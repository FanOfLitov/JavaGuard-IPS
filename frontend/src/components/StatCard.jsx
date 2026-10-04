export default function StatCard({
                                     title,
                                     value
                                 }) {

    return (
        <article className="stat-card">

            <span className="stat-card-title">
                {title}
            </span>

            <strong className="stat-card-value">
                {value ?? '-'}
            </strong>

        </article>
    )
}