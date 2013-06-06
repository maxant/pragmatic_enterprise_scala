/*
 *   Copyright 2013 Ant Kutschera
 *   
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package ch.maxant.scalabook.shop.common.services

import java.io._
import java.lang.System.{currentTimeMillis => now}
import java.sql.Timestamp
import java.util.{List => JList}
import java.util.logging.Logger
import scala.collection.mutable.ListBuffer
import ch.maxant.scalabook.shop.bom._
import ch.maxant.scalabook.shop.common.persistence.jpa.User
import ch.maxant.scalabook.shop.common.services.qualifiers.ModelDestroyed
import ch.maxant.scalabook.shop.ui.Model
import javax.annotation.Resource
import javax.ejb._
import javax.enterprise.event.Event
import javax.inject.Inject
import javax.persistence.Access
import javax.persistence.Entity
import javax.sql.DataSource

@Stateless
@LocalBean
class SessionService {

    private val VersionNumber = "1.3"
    
    @Inject var log: Logger = null
    @Resource(name="java:/jdbc/MyXaDS") var datasource: DataSource = null
    @Inject @ModelDestroyed var modelDestroyedEvent: Event[SessionService.Wrapper] = null
    
    def get(key: String) = {
        var ret: SessionService.Wrapper = null
        val conn = datasource.getConnection()
        val stmt = conn.prepareStatement("SELECT VAL FROM SESSION_CACHE WHERE ID = ?")
        stmt.setString(1, key)
        val rs = stmt.executeQuery()
        if(rs.next()){
            ret = deserialise(rs.getBinaryStream(1))
        }
        rs.close()
        stmt.close()
        conn.close()
        ret
    }
    
    def put(key: String, model: Model){

        //attempt an update and if it fails, do an insert.
        //that is quicker than doing a select first to check if an insert or update is required.
        //because selecting first will always cause two statements to be carried out. update attempt
        //strategy causes just one, normally. only when a session is started are two sqls carried out.
        
        //use xml to serialise the model so that we have the opportunity to 
        //transform it later, if the model structure changes between versions of the server.
        
        //val xml = toXML(model)
        //var is = new ByteArrayInputStream(xml.getBytes)
        var is = serialise(model)
        
        val conn = datasource.getConnection()
        var stmt = conn.prepareStatement("UPDATE SESSION_CACHE SET VAL = ?, LAST_USED = ? WHERE ID = ?")
        stmt.setBinaryStream(1, is)
        stmt.setTimestamp(2, new Timestamp(now))
        stmt.setString(3, key)
        val cnt = stmt.executeUpdate
        if(cnt == 0){
            stmt.close()
            is.reset()
            stmt = conn.prepareStatement("INSERT INTO SESSION_CACHE(ID, VAL, LAST_USED) VALUES (?, ?, ?)")
            stmt.setString(1, key)
            stmt.setBinaryStream(2, is)
            stmt.setTimestamp(3, new Timestamp(now))
            stmt.executeUpdate
        }
        is.close()
        stmt.close()
        conn.close()
    }
    
    @Schedule(minute="*/1", hour="*", persistent=false)
    def cleanup() = {
        //TODO first select, then fire event, so that others can respond to a cleaned up event!
        val conn = datasource.getConnection()
        var stmt = conn.prepareStatement("SELECT ID, VAL FROM SESSION_CACHE WHERE LAST_USED < ?")
        stmt.setTimestamp(1, new Timestamp(now - (4*3600000L))) //4 hours
        val rs = stmt.executeQuery()
        val ids = new ListBuffer[String]()
        while(rs.next()){
            val id = rs.getString(1)
            try{
                val model = deserialise(rs.getBinaryStream(2))
                modelDestroyedEvent.fire(model)
            }catch{
                case ice: java.io.InvalidClassException => //thats a shame, well we cant do anything with the model, so tough titties to any listeners! delete it anyway!
            }
            ids += id
            var stmt2 = conn.prepareStatement("DELETE FROM SESSION_CACHE WHERE ID = ?")
            stmt2.setString(1, id)
            stmt2.executeUpdate()
            stmt2.close()
        }
        log.info("Removed " + ids.size + " expired sessions from the persistent cache")
        stmt.close()
        conn.close()
    }    

    private def serialise(m: Model) = {
        val baos = new ByteArrayOutputStream()
        val oos = new ObjectOutputStream(baos)
        val w = new SessionService.Wrapper(
                        m.user,
                        m.selectedEvent,
                        m.reservations,
                        m.order,
                        m.insurance,
                        m.insuranceSelected
                    )
        oos.writeObject(w)
        new ByteArrayInputStream(baos.toByteArray())
    }
    
    //seems to require jboss-client.jar since there is an interceptor 
    //looking for class org.jboss.logmanager.SerializedLogger...
    private def deserialise(is: InputStream) = {
        
        System.setProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
        
        val ois = new ObjectInputStream(is)
        val o = ois.readObject
        o.asInstanceOf[SessionService.Wrapper]
    }
    
//    private def getFromStream(is: InputStream) = {
//        val baos = new ByteArrayOutputStream
//        var curr = is.read
//        while(curr != -1){
//            baos.write(curr)
//            curr = is.read
//        }
//        is.close
//
//        val xml = new String(baos.toByteArray)
//
//        fromXML(xml)
//    }
//                        
//    private def toXML(m: Model) = {
//        val w = new SessionService.Wrapper(
//                        m.teasers,
//                        m.user,
//                        m.selectedEvent,
//                        m.reservations,
//                        m.order,
//                        m.insurance,
//                        m.insuranceSelected
//                    )
//
//        "<model version='" + VersionNumber + "'>" + new XStream().toXML(w) + "</model>"
//    }
//
//    private def fromXML(xml: String): SessionService.Wrapper = {
//
//        //        "<model version='1.3'>" + new XStream().toXML(w) + "</model>"
//        var version = xml.substring(7, 100)
//        if(!version.startsWith("version")) return null //no idea what this is!
//
//        val idx = version.indexOf("'", 9)
//        if(idx < 0) return null //no idea what this is!
//        version = version.substring(9, idx)
//
//        if(version != VersionNumber){
//            //TODO some kind of xml transformation so that it becomes compatible!
//            //for now, just return null, since this is simply a demo
//            return null
//        }else{
//            var idx1 = xml.indexOf(">")
//            var idx2 = xml.lastIndexOf("<")
//            var subXml = xml.substring(idx1+1, idx2)
//            new XStream().fromXML(subXml).asInstanceOf[SessionService.Wrapper]
//        }
//    }
                        
}

object SessionService {
    case class Wrapper(
        user: User,
        selectedEvent: EventOffer,
        reservations: JList[Reservation],
        order: Order,
        insurance: Reservation,
        insuranceSelected: Boolean )
}
