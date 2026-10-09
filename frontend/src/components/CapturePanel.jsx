import { useState } from 'react'


export default function CapturePanel({
                                         capture,
                                         onStart,
                                         onStop
                                     }) {
    const [interfaceName, setInterfaceName] = useState('lo')


    async function handleStart() {
        const value = interfaceName.trim()

        if (!value) {
            return
        }

        await onStart(value)
    }


    const isRunning = capture?.running ?? false


    return (
        <section className="capture-panel">

            <div className="capture-info">

                <div className="capture-title-row">

                    <h2>Packet Capture</h2>

                    <span
                        className={
                            isRunning
                                ? 'capture-badge running'
                                : 'capture-badge stopped'
                        }
                    >
                        {
                            isRunning
                                ? 'RUNNING'
                                : 'STOPPED'
                        }
                    </span>

                </div>


                <p>
                    Active interface:{' '}

                    <strong>
                        {capture?.interfaceName ?? 'None'}
                    </strong>
                </p>

            </div>


            <div className="capture-controls">

                <input
                    type="text"
                    value={interfaceName}
                    placeholder="Network interface"
                    onChange={
                        event =>
                            setInterfaceName(event.target.value)
                    }
                />


                <button
                    type="button"
                    className="button start-button"
                    onClick={handleStart}
                    disabled={isRunning}
                >
                    Start
                </button>


                <button
                    type="button"
                    className="button stop-button"
                    onClick={onStop}
                    disabled={!isRunning}
                >
                    Stop
                </button>

            </div>

        </section>
    )
}