package us.brainstormz.robotTwo.localTests

import us.brainstormz.pid.PID


fun main() {
    TestRunner.runTests(mapOf(
            "PID happy path" to {
                // given
                val p = PID("happy pid", kp = 1.0, now = 0)

                // when
                val result = p.calcPID(1, error = 1.0, now = 1)

                // then
                assertEquals(1.0, result)
                Passed
            },
            "when using just P, it's is not affected by time" to {
                // given
                val p = PID("happy pid", kp = 1.0, now = 0)

                // when
                val result = p.calcPID(1, error = 1.0, now = 10)

                // then
                assertEquals(1.0, result)
                Passed
            },
            "when using i, it's is affected by time" to {
                // given
                val p = PID("happy pid", ki = 1.0, now = 0)

                // when
                val result = p.calcPID(1, error = 1.0, now = 1)

                // then
                assertEquals(1.0, result)
                Passed
            },
            "time cannot go backwards" to {
                // given
                val p = PID("marty", ki = 1.0, now = 0)

                // when
                val result = try{
                    p.calcPID(1, error = 1.0, now = -1)
                    null
                }catch(t:Throwable){
                    t
                }

                // then
                assertNotNull(result)
                Passed
            },
            "when the error persists, the power increases" to {
                // given
                val p = PID("try harder pid", ki = 1.0, now = 0, limits = -100.0..100.0)

                // when
                val result = listOf(
                        p.calcPID(1, error = 0.0, now = 1),
                        p.calcPID(1, error = 0.5, now = 2),
                        p.calcPID(1, error = 1.0, now = 3),
                )

                // then
                assertEquals(listOf(
                        0.0,
                        0.5,
                        1.5
                ), result)
                Passed
            },
            "when the target changes, everything resets" to {
                // given
                val p = PID(ki = 1.0, now = 0, limits = -100.0..100.0, name="Super Test PID")

                // when
                val result = listOf(
                        p.calcPID("foo", error = 0.0, now = 1),
                        p.calcPID("foo", error = 0.5, now = 2),
                        p.calcPID("bar", error = 1.0, now = 3),
                )

                // then
                assertEquals(listOf(
                        0.0,
                        0.5,
                        1.0
                ), result)
                Passed
            },
    ))
}
