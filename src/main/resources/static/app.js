const API_BASE = "/api/v1";


const elements = {

    connectionStatus:
        document.getElementById("connectionStatus"),

    captureState:
        document.getElementById("captureState"),

    activeInterface:
        document.getElementById("activeInterface"),

    processedPackets:
        document.getElementById("processedPackets"),

    droppedEvents:
        document.getElementById("droppedEvents"),

    packetsPerSecond:
        document.getElementById("packetsPerSecond"),

    totalAlerts:
        document.getElementById("totalAlerts"),

    alertsLast24Hours:
        document.getElementById("alertsLast24Hours"),

    protocolStatistics:
        document.getElementById("protocolStatistics"),

    threatStatistics:
        document.getElementById("threatStatistics"),

    severityStatistics:
        document.getElementById("severityStatistics"),

    topSourceIps:
        document.getElementById("topSourceIps"),

    alertsTableBody:
        document.getElementById("alertsTableBody"),

    interfaceInput:
        document.getElementById("interfaceInput"),

    startCaptureButton:
        document.getElementById("startCaptureButton"),

    stopCaptureButton:
        document.getElementById("stopCaptureButton"),

    refreshButton:
        document.getElementById("refreshButton"),

    errorMessage:
        document.getElementById("errorMessage")
};


async function request(
    path,
    options = {}
) {

    const response =
        await fetch(
            `${API_BASE}${path}`,
            options
        );


    const text =
        await response.text();


    let body = null;


    if (text) {

        try {

            body =
                JSON.parse(text);

        } catch {

            body = text;
        }
    }


    if (!response.ok) {

        const message =
            body?.message
            ?? `HTTP ${response.status}`;


        throw new Error(message);
    }


    return body;
}


async function loadDashboard() {

    try {

        const data =
            await request(
                "/dashboard/summary"
            );


        renderDashboard(data);


        elements.connectionStatus.textContent =
            "Connected";


        hideError();

    } catch (error) {

        elements.connectionStatus.textContent =
            "Disconnected";


        showError(
            error.message
        );
    }
}


function renderDashboard(data) {

    const capture =
        data.capture;

    const traffic =
        data.traffic;

    const security =
        data.security;


    elements.captureState.textContent =
        capture.running
            ? "RUNNING"
            : "STOPPED";


    elements.activeInterface.textContent =
        capture.interfaceName
        ?? "-";


    elements.processedPackets.textContent =
        capture.processedEvents;


    elements.droppedEvents.textContent =
        capture.droppedEvents;


    elements.packetsPerSecond.textContent =
        traffic.packetsPerSecond;


    elements.totalAlerts.textContent =
        security.totalAlerts;


    elements.alertsLast24Hours.textContent =
        security.alertsLast24Hours;


    renderMap(
        elements.protocolStatistics,
        traffic.protocolCounts
    );


    renderMap(
        elements.threatStatistics,
        security.byThreatType
    );


    renderMap(
        elements.severityStatistics,
        security.bySeverity
    );


    renderTopSourceIps(
        security.topSourceIps
    );


    renderAlerts(
        data.recentAlerts
    );
}


function renderMap(
    container,
    values
) {

    container.innerHTML = "";


    const entries =
        Object.entries(
            values ?? {}
        );


    if (entries.length === 0) {

        container.innerHTML =
            `<div class="stat-row">
                <span>No data</span>
            </div>`;

        return;
    }


    for (const [name, value] of entries) {

        const row =
            document.createElement("div");

        row.className =
            "stat-row";


        row.innerHTML =
            `
            <span>${escapeHtml(name)}</span>
            <strong>${value}</strong>
            `;


        container.appendChild(row);
    }
}


function renderTopSourceIps(
    sourceIps
) {

    elements.topSourceIps.innerHTML =
        "";


    if (
        !sourceIps
        ||
        sourceIps.length === 0
    ) {

        elements.topSourceIps.innerHTML =
            `<div class="stat-row">
                <span>No data</span>
            </div>`;

        return;
    }


    for (const item of sourceIps) {

        const row =
            document.createElement("div");

        row.className =
            "stat-row";


        row.innerHTML =
            `
            <span>
                ${escapeHtml(item.sourceIp)}
            </span>

            <strong>
                ${item.count}
            </strong>
            `;


        elements.topSourceIps
            .appendChild(row);
    }
}


function renderAlerts(
    alerts
) {

    elements.alertsTableBody.innerHTML =
        "";


    if (
        !alerts
        ||
        alerts.length === 0
    ) {

        elements.alertsTableBody.innerHTML =
            `
            <tr>
                <td colspan="7">
                    No alerts
                </td>
            </tr>
            `;

        return;
    }


    for (const alert of alerts) {

        const row =
            document.createElement("tr");


        row.innerHTML =
            `
            <td>
                ${formatTime(alert.timestamp)}
            </td>

            <td>
                ${escapeHtml(alert.type)}
            </td>

            <td>
                ${escapeHtml(alert.severity)}
            </td>

            <td>
                ${escapeHtml(alert.sourceIp)}
            </td>

            <td>
                ${escapeHtml(alert.destinationIp)}
            </td>

            <td>
                ${alert.evidenceCount}
            </td>

            <td>
                ${escapeHtml(alert.description)}
            </td>
            `;


        elements.alertsTableBody
            .appendChild(row);
    }
}


async function startCapture() {

    const interfaceName =
        elements
            .interfaceInput
            .value
            .trim();


    if (!interfaceName) {

        showError(
            "Network interface is required"
        );

        return;
    }


    try {

        await request(
            `/capture/start/${
                encodeURIComponent(
                    interfaceName
                )
            }`,
            {
                method: "POST"
            }
        );


        await loadDashboard();

    } catch (error) {

        showError(
            error.message
        );
    }
}


async function stopCapture() {

    try {

        await request(
            "/capture/stop",
            {
                method: "POST"
            }
        );


        await loadDashboard();

    } catch (error) {

        showError(
            error.message
        );
    }
}


function formatTime(
    timestamp
) {

    if (!timestamp) {
        return "-";
    }


    return new Date(
        timestamp
    ).toLocaleString();
}


function escapeHtml(
    value
) {

    if (
        value === null
        ||
        value === undefined
    ) {

        return "";
    }


    const element =
        document.createElement("div");


    element.textContent =
        String(value);


    return element.innerHTML;
}


function showError(
    message
) {

    elements.errorMessage.textContent =
        message;


    elements.errorMessage.classList
        .remove("hidden");
}


function hideError() {

    elements.errorMessage.classList
        .add("hidden");
}


elements.startCaptureButton
    .addEventListener(
        "click",
        startCapture
    );


elements.stopCaptureButton
    .addEventListener(
        "click",
        stopCapture
    );


elements.refreshButton
    .addEventListener(
        "click",
        loadDashboard
    );


loadDashboard();


setInterval(
    loadDashboard,
    2000
);