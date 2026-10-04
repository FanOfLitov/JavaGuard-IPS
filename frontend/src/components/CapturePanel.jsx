import { useState } from 'react'


export default function CapturePanel({
                                         capture,
                                         onStart,
                                         onStop
                                     }) {

    const [interfaceName, setInterfaceName] =
        useState('lo')


    async function handleStart() {

        if (!interfaceName.trim()) {
            return
        }

        await onStart(
            interfaceName.trim()
        )
    }


    return (
        <section className="capture-panel">

            <div>

                <h2>
                    Packet Capture
                </h2>

                <p>
                    Active interface:{' '}

                    <strong>
                        {
                            capture?.interfaceName
                            ?? '-'
                        }
                    </strong>
                </p>

            </div>


            <div className="capture-controls">

                <input
                    value={interfaceName}

                    onChange={
                        event =>
                            setInterfaceName(
                                event.target.value
                            )
                    }

                    placeholder="Network interface"
                />


                <button
                    className="button start-button"
                    onClick={handleStart}
                >
                    Start
                </button>


                <button
                    className="button stop-button"
                    onClick={onStop}
                >
                    Stop
                </button>

            </div>

        </section>
    )
}