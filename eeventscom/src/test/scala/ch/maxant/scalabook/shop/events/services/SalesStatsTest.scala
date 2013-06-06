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
package ch.maxant.scalabook.shop.events.services
import org.junit.Test
import org.junit.Assert._
import java.net.URL
import java.net.URLEncoder
import java.io.ByteArrayOutputStream
import scala.util.Random

class SalesStatsTest {

    @Test
    def testSale {
        val email = "john@maxant.ch"
        val uid = "A"
        val result = doSale(uid, email)
		assertEquals("<result>OK</result>", result)
    }

    @Test
    def bulkLoad {
        var start = System.currentTimeMillis
        var a = 1
        for(i <- 1 to 1000000){
            try{
	            if(i % 1000 == 0){
	                println(i + " sales, avg " + ((System.currentTimeMillis()-start)/1000.0) + " ms/sale, including all queries")
	                start = System.currentTimeMillis()
	            }
	            if(i % 10000 == 0){
	                a += 1 //so that we can book events in groups, so that we dont have to wait for ever to get matches, lets do them in groups, 1% at a time
	            }
	
	            //300 seats a gig, average
	            //86400 tickets a day => 288 gigs a day
	            //we want to create 30 days worth of data, so that means 8640 gigs
	            //user buys on average 2 tickets per gig, to 3 gigs => 432,000 users.
	            
	            val uid = "event-" + a + "-" + Random.nextInt(86)
	    		val email = Random.nextInt(43200) + "@maxant.ch"
	            
	            if(i % 100 == 0){
	                val start2 = System.currentTimeMillis()
	            	val res = query(uid, email)
	            	println("queried " + uid + "/" + email + " in " + (System.currentTimeMillis()-start2) + "ms: " + res)
	            	val start3 = System.currentTimeMillis()
	            	val res2 = query(uid, email)
	            	println("queried in " + (System.currentTimeMillis()-start3) + "ms: " + res2)
	            }
	            
	            doSale(uid, email)
            }catch{
            	case e: Exception => println("exception: " + e.getMessage)
            }
        }
    }
    
    private def doSale(uid: String, email: String) {
        val url = new URL("http://localhost:9000/addSale?eventUid=" + URLEncoder.encode(uid) + "&email=" + URLEncoder.encode(email))
        val is = url.openStream()
		val baos = new ByteArrayOutputStream
		try {
			var curr = is.read()
			while(curr >= 0){
			    baos.write(curr)
			    curr = is.read()
			}
		} finally {
			is.close()
		}
		
		baos.toString()
    }

    private def query(uid: String, email: String) {
        val url = new URL("http://localhost:9000/customersWhoBought/" + URLEncoder.encode(uid) + "/" + URLEncoder.encode(email))
        val is = url.openStream()
		val baos = new ByteArrayOutputStream
		try {
			var curr = is.read()
			while(curr >= 0){
			    baos.write(curr)
			    curr = is.read()
			}
		} finally {
			is.close()
		}
		
		baos.toString()
    }

}