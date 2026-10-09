const API_BASE = '/api/v1'


async function request(path, options = {}) {

    const response =
        await fetch(
            `${API_BASE}${path}`,
            options
        )


    const text =
        await response.text()


    let body = null


    if (text) {

        try {
            body = JSON.parse(text)

        } catch {
            body = text
        }
    }


    if (!response.ok) {

        const message =
            body?.message
            ?? `HTTP ${response.status}`


        throw new Error(message)
    }


    return body
}


export function getDashboardSummary() {

    return request(
        '/dashboard/summary'
    )
}


export function startCapture(interfaceName) {

    return request(
        `/capture/start/${encodeURIComponent(interfaceName)}`,
        {
            method: 'POST'
        }
    )
}


export function stopCapture() {

    return request(
        '/capture/stop',
        {
            method: 'POST'
        }
    )
}


export function searchAlerts({
                                 type,
                                 severity,
                                 sourceIp,
                                 destinationIp,
                                 page = 0,
                                 size = 20
                             }) {

    const params =
        new URLSearchParams()


    if (type) {
        params.set('type', type)
    }


    if (severity) {
        params.set(
            'severity',
            severity
        )
    }


    if (sourceIp) {
        params.set(
            'sourceIp',
            sourceIp
        )
    }


    if (destinationIp) {
        params.set(
            'destinationIp',
            destinationIp
        )
    }


    params.set(
        'page',
        String(page)
    )


    params.set(
        'size',
        String(size)
    )


    return request(
        `/alerts/search?${params.toString()}`
    )
}