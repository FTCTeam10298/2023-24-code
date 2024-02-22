package us.brainstormz.utils

import fi.iki.elonen.NanoHTTPD
import org.firstinspires.ftc.robotcore.external.Func
import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.faux.PrintlnTelemetry
import java.io.ByteArrayOutputStream

/**
 * Potential future improvement ... make this start-up automatically somehow
 */
class ConfigServer(
        val port:Int,
        private val get:()->String,
        private val update:(String)->Unit,
        private val getInfoToPrint: ()->String
) : NanoHTTPD(port) {

    init {
        start(SOCKET_READ_TIMEOUT, false)
        println("starting server")
    }

    private fun defaultResponse() = newFixedLengthResponse("""<html>
                |   <script type="text/javascript" src="app.js"></script>
                |   <textarea style="width:100%;height:80%"></textarea>
                |   <button>Submut Dawg</button>
                |   <p id="info" style="width:100%;height:50%;"></p>
                |   <script>
                |   document.querySelector("button").onclick = function(){
                |       send(document.querySelector("textarea").value, refresh)
                |   }
                |   refresh()
                |   </script>
                |</html>
            """.trimMargin())
    override fun serve(session: IHTTPSession): Response {

        val stuff = session.method to session.uri

        val infoAsUsableString = getInfoToPrint().fold("") {acc, it ->
            acc + if (it == '\n') {
                "\\n"
            } else {
                it
            }
        }

        println("request is $stuff")

        return when(stuff){
            (Method.GET to "/app.js") -> newFixedLengthResponse("""
                function send(config, onComplete){
                    const xhr = new XMLHttpRequest();
                    xhr.open("POST", "/config", true);
    
                    // Send the proper header information along with the request
                    xhr.setRequestHeader("Content-Type", "application/json");
    
                    xhr.onreadystatechange = () => {
                      // Call a function when the state changes.
                      if (xhr.readyState === XMLHttpRequest.DONE && xhr.status === 200) {
                        // Request finished. Do processing here.
                        console.log("Done: got ", xhr.responseText)
                        onComplete()
                      }
                    };
                    xhr.send(config);
                }
                function refresh(){
                    fetch("/config").then(r=>r.text()).then(function(text){
                         document.querySelector("textarea").value = text
                    })
                    document.getElementById("info").innerText = '$infoAsUsableString';
                }
            """.trimIndent())

            (Method.POST to "/config") ->{
                println("I got something: ")
                val contentLength = Integer.valueOf(session.headers["content-length"])


                val buffer = ByteArrayOutputStream()
                var numRead = 0;
                var keepReading = true
                while(keepReading){
                    val b = session.inputStream.read()
                    numRead +=1
                    if(b==-1){
                        keepReading = false
                    }else{
                        buffer.write(b)
                        if(numRead == contentLength){
                            keepReading = false
                        }
                    }
                }
                val json = buffer.toByteArray().decodeToString()
                println("Here is the text $json")
                this.update(json)
                println("Config is now ${get()}")
                newFixedLengthResponse(get())

            }
            (Method.GET to "/config") ->{
                newFixedLengthResponse(get())
            }
            else -> defaultResponse()
        }
    }

}

class ConfigServerTelemetry: Telemetry {
    private val linesQueue: MutableList<String> = mutableListOf()
    var screenOfLines: List<String> = mutableListOf()
    override fun addData(caption: String?, format: String?, vararg args: Any?): Telemetry.Item {
        addLine(caption)
        return PrintlnTelemetry.FauxItem()
    }

    override fun addData(caption: String?, value: Any?): Telemetry.Item {
        return addData(caption, null, value)
    }

    override fun <T : Any?> addData(caption: String?, valueProducer: Func<T>?): Telemetry.Item? {
        addLine(caption)
        return null
    }

    override fun <T : Any?> addData(caption: String?, format: String?, valueProducer: Func<T>?): Telemetry.Item {
        addLine(caption)
        return PrintlnTelemetry.FauxItem()
    }

    override fun removeItem(item: Telemetry.Item?): Boolean {
        return true
    }

    override fun clear() {
        linesQueue.clear()
    }

    override fun clearAll() {
        linesQueue.clear()
    }

    override fun addAction(action: Runnable?): Any {
        return false
    }

    override fun removeAction(token: Any?): Boolean {
        return false
    }

    override fun speak(text: String?) {

    }

    override fun speak(text: String?, languageCode: String?, countryCode: String?) {

    }

    override fun update(): Boolean {
        screenOfLines = linesQueue.toList()
        linesQueue.clear()
        return true
    }

    override fun addLine(): Telemetry.Line {
        return addLine(lineCaption = null)
    }

    override fun addLine(lineCaption: String?): Telemetry.Line {
        linesQueue.add(lineCaption + "\n")
        return PrintlnTelemetry.FauxLine()
    }

    override fun removeLine(line: Telemetry.Line?): Boolean {
        return true
    }

    override fun isAutoClear(): Boolean {
        return true
    }

    override fun setAutoClear(autoClear: Boolean) {

    }

    override fun getMsTransmissionInterval(): Int {
        return 0
    }

    override fun setMsTransmissionInterval(msTransmissionInterval: Int) {

    }

    override fun getItemSeparator(): String {
        return "\n"
    }

    override fun setItemSeparator(itemSeparator: String?) {

    }

    override fun getCaptionValueSeparator(): String {
        return ""
    }

    override fun setCaptionValueSeparator(captionValueSeparator: String?) {
    }

    override fun setDisplayFormat(displayFormat: Telemetry.DisplayFormat?) {
    }

    override fun log(): Telemetry.Log {
        return PrintlnTelemetry.FauxLog()
    }

}