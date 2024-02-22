package us.brainstormz.faux

import org.firstinspires.ftc.robotcore.external.Func
import org.firstinspires.ftc.robotcore.external.Telemetry

class PrintlnTelemetry: Telemetry {

    private val telemetryPrintSignature = "Telemetry: "
    private fun printCaption(caption: String?) {
        println(telemetryPrintSignature + caption)
    }
    private val phoItem = PhoItem()
    private val phoLine = PhoLine()

    override fun addData(caption: String?, format: String?, vararg args: Any?): Telemetry.Item {
        printCaption(caption)
        return phoItem
    }

    override fun addData(caption: String?, value: Any?): Telemetry.Item {
        printCaption(caption)
        return phoItem
    }

    override fun <T : Any?> addData(caption: String?, valueProducer: Func<T>?): Telemetry.Item {
        printCaption(caption)
        return phoItem
    }

    override fun <T : Any?> addData(caption: String?, format: String?, valueProducer: Func<T>?): Telemetry.Item {
        printCaption(caption)
        return phoItem
    }

    override fun removeItem(item: Telemetry.Item?): Boolean {
        TODO("Not yet implemented")
    }

    override fun clear() {
        TODO("Not yet implemented")
    }

    override fun clearAll() {
        printCaption("Clearing")
    }

    override fun addAction(action: Runnable?): Any {
        TODO("Not yet implemented")
    }

    override fun removeAction(token: Any?): Boolean {
        TODO("Not yet implemented")
    }

    override fun speak(text: String?) {
        TODO("Not yet implemented")
    }

    override fun speak(text: String?, languageCode: String?, countryCode: String?) {
        TODO("Not yet implemented")
    }

    override fun update(): Boolean {
        printCaption("%%Telemetry.Update%%")
        return true
    }

    override fun addLine(): Telemetry.Line {
        printCaption("")
        return phoLine
    }

    override fun addLine(lineCaption: String?): Telemetry.Line {
        printCaption(lineCaption)
        return phoLine
    }

    override fun removeLine(line: Telemetry.Line?): Boolean {
        TODO("Not yet implemented")
    }

    override fun isAutoClear(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setAutoClear(autoClear: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getMsTransmissionInterval(): Int {
        TODO("Not yet implemented")
    }

    override fun setMsTransmissionInterval(msTransmissionInterval: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemSeparator(): String {
        TODO("Not yet implemented")
    }

    override fun setItemSeparator(itemSeparator: String?) {
        TODO("Not yet implemented")
    }

    override fun getCaptionValueSeparator(): String {
        TODO("Not yet implemented")
    }

    override fun setCaptionValueSeparator(captionValueSeparator: String?) {
        TODO("Not yet implemented")
    }

    override fun setDisplayFormat(displayFormat: Telemetry.DisplayFormat?) {
        TODO("Not yet implemented")
    }

    override fun log(): Telemetry.Log {
        TODO("Not yet implemented")
    }



    class PhoItem: Telemetry.Item {
        override fun getCaption(): String {
            TODO("Not yet implemented")
        }

        override fun setCaption(caption: String?): Telemetry.Item {
            TODO("Not yet implemented")
        }

        override fun setValue(format: String?, vararg args: Any?): Telemetry.Item {
            TODO("Not yet implemented")
        }

        override fun setValue(value: Any?): Telemetry.Item {
            TODO("Not yet implemented")
        }

        override fun <T : Any?> setValue(valueProducer: Func<T>?): Telemetry.Item {
            TODO("Not yet implemented")
        }

        override fun <T : Any?> setValue(format: String?, valueProducer: Func<T>?): Telemetry.Item {
            TODO("Not yet implemented")
        }

        override fun setRetained(retained: Boolean?): Telemetry.Item {
            TODO("Not yet implemented")
        }

        override fun isRetained(): Boolean {
            TODO("Not yet implemented")
        }

        override fun addData(caption: String?, format: String?, vararg args: Any?): Telemetry.Item {
            TODO("Not yet implemented")
        }

        override fun addData(caption: String?, value: Any?): Telemetry.Item {
            TODO("Not yet implemented")
        }

        override fun <T : Any?> addData(caption: String?, valueProducer: Func<T>?): Telemetry.Item {
            TODO("Not yet implemented")
        }

        override fun <T : Any?> addData(caption: String?, format: String?, valueProducer: Func<T>?): Telemetry.Item {
            TODO("Not yet implemented")
        }

    }

    class PhoLine: Telemetry.Line {
        override fun addData(caption: String?, format: String?, vararg args: Any?): Telemetry.Item {
            TODO("Not yet implemented")
        }

        override fun addData(caption: String?, value: Any?): Telemetry.Item {
            TODO("Not yet implemented")
        }

        override fun <T : Any?> addData(caption: String?, valueProducer: Func<T>?): Telemetry.Item {
            TODO("Not yet implemented")
        }

        override fun <T : Any?> addData(caption: String?, format: String?, valueProducer: Func<T>?): Telemetry.Item {
            TODO("Not yet implemented")
        }

    }

    class FauxLog: Telemetry.Log {
        override fun getCapacity(): Int {
            return 0
        }

        override fun setCapacity(capacity: Int) {
        }

        override fun getDisplayOrder(): Telemetry.Log.DisplayOrder {
            return Telemetry.Log.DisplayOrder.NEWEST_FIRST
        }

        override fun setDisplayOrder(displayOrder: Telemetry.Log.DisplayOrder?) {
        }

        override fun add(entry: String?) {

        }

        override fun add(format: String?, vararg args: Any?) {

        }

        override fun clear() {

        }

    }
}