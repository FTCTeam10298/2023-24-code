package us.brainstormz.utils

import fi.iki.elonen.NanoHTTPD
import java.io.ByteArrayOutputStream

/**
 * Potential future improvement ... make this start-up automatically somehow
 */
class ConfigServer(val port:Int, private val get:()->String, private val update:(String)->Unit) : NanoHTTPD(port) {

    init {
        start(SOCKET_READ_TIMEOUT, false)
    }

    private fun defaultResponse() = newFixedLengthResponse("""<html>
                |   <script type="text/javascript" src="app.js"></script>
                |   <textarea style="width:100%;height:80%"></textarea>
                |   <button>Submut Dawg</button>
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