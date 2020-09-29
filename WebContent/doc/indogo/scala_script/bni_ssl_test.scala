import javax.net.ssl.SSLContext
import java.security.KeyStore
import java.io.FileInputStream
import java.security.cert.X509Certificate
import java.security.cert.CertificateException
import org.apache.http.ssl.TrustStrategy
import org.apache.http.conn.ssl.SSLConnectionSocketFactory

val keyStoreFileLocation = "/var/tomcat/indogo_dev.jks"
val keyStore = KeyStore.getInstance("jks")
val stream = new FileInputStream(keyStoreFileLocation)
keyStore.load(stream, "password".toCharArray)
stream.close

class MyTrustStrategy extends TrustStrategy {
    def isTrusted(certs: Array[X509Certificate], arg1: String): Boolean = {
        true
    }
}

val sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(keyStore, new MyTrustStrategy()).build()
val sslsf = new SSLConnectionSocketFactory(sslContext)
val r = org.apache.http.config.RegistryBuilder.create[org.apache.http.conn.socket.ConnectionSocketFactory]().register("https", sslsf).build()
val cm = new org.apache.http.impl.conn.PoolingHttpClientConnectionManager(r)
val httpClient = org.apache.http.impl.client.HttpClients.custom().setConnectionManager(cm).build()

val stringEntity = new org.apache.http.entity.StringEntity("<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"><s:Body xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"><processPO xmlns=\"http://service.bni.co.id/remm\"><header xmlns=\"\"><clientId>INDOGO</clientId><signature>K1SMhjKDMH3TmfZUgFdtzFizTxjnZvOBe0clAQWWWbeuc8Qg4XkTNeDGhVIibs4WxeWyCsd6ZPbvuvMLYhRY+oqSVAYtFN8WwQsHztbIsN9IiVJGAz4S+SiBscaY7gYK06ZJsR9H6pBo94ZferPx3xDILsOPUxPL5d+q6ndcfe0=</signature></header><paymentOrder xmlns=\"\"><refNumber>null</refNumber><serviceType>INTERBANK</serviceType><trxDate>2017-05-12T00:00:00</trxDate><currency>IDR</currency><amount>420000</amount><orderingName>AAM MARPUAH</orderingName><orderingAddress1>TAIWAN</orderingAddress1><orderingAddress2/><orderingPhoneNumber/><beneficiaryAccount>420201000083501</beneficiaryAccount><beneficiaryName>TARSIWAN</beneficiaryName><beneficiaryAddress1>INDONESIA</beneficiaryAddress1><beneficiaryAddress2/><beneficiaryPhoneNumber></beneficiaryPhoneNumber><acctWithInstcode>A</acctWithInstcode><acctWithInstName>002</acctWithInstName><acctWithInstAddress1/><acctWithInstAddress2/><acctWithInstAddress3/><detailPayment1/><detailPayment2/><detailCharges>OUR</detailCharges></paymentOrder></processPO></s:Body></s:Envelope>")
val httpPost = new org.apache.http.client.methods.HttpPost("https://remitapi.bni.co.id:55001/remittance/incoming/indogo")
httpPost.addHeader("Content-Type", "text/xml;charset=utf-8")
httpPost.addHeader("SOAPAction", "")
httpPost.setEntity(stringEntity)
val response = httpClient.execute(httpPost)
val entity = response.getEntity()
val inputStream = entity.getContent()
val replyString = org.apache.commons.io.IOUtils.toString(inputStream, "UTF-8")
org.apache.http.util.EntityUtils.consume(entity)
response.close
httpClient.close
