/*
  *  Copyright (C) 2004-2007 FhG Fokus
  *
  * This file is part of Open IMS Core - an open source IMS CSCFs & HSS
  * implementation
  *
  * Open IMS Core is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * For a license to use the Open IMS Core software under conditions
  * other than those described here, or to purchase support for this
  * software, please contact Fraunhofer FOKUS by e-mail at the following
  * addresses:
  *     info@open-ims.org
  *
  * Open IMS Core is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * It has to be noted that this Open Source IMS Core System is not
  * intended to become or act as a product in a commercial context! Its
  * sole purpose is to provide an IMS core reference implementation for
  * IMS technology testing and IMS application prototyping for research
  * purposes, typically performed in IMS test-beds.
  *
  * Users of the Open Source IMS Core System have to be aware that IMS
  * technology may be subject of patents and licence terms, as being
  * specified within the various IMS-related IETF, ITU-T, ETSI, and 3GPP
  * standards. Thus all Open IMS Core users have to take notice of this
  * fact and have to agree to check out carefully before installing,
  * using and extending the Open Source IMS Core System, if related
  * patents and licenses may become applicable to the intended usage
  * context. 
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA  
  * 
  */

package org.freeims.hss.server.db.op;

import java.util.List;

import org.apache.log4j.Logger;
import org.freeims.hss.server.model.ChargingInfo;
import org.freeims.hss.server.model.TP;
import org.freeims.hss.server.model.VisitedNetwork;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 * @author adp dot fokus dot fraunhofer dot de 
 * Adrian Popescu / FOKUS Fraunhofer Institute
 */
public class VisitedNetwork_DAO {
	private static Logger logger = Logger.getLogger(VisitedNetwork_DAO.class);
	
	public static void insert(Session session, VisitedNetwork visited_network){
		session.save(visited_network);
	}

	public static void update(Session session, VisitedNetwork visited_network){
		session.save(visited_network);
	}
	
	public static VisitedNetwork get_by_ID(Session session, int id){
		Query query;
		query = session.createSQLQuery("select * from visited_network where id=?")
			.addEntity(VisitedNetwork.class);
		query.setInteger(0, id);

		return (VisitedNetwork) query.uniqueResult();
	}	
	
	
	public static VisitedNetwork get_by_Identity(Session session, String identity){
		Query query;
		query = session.createSQLQuery("select * from visited_network where identity like ?")
			.addEntity(VisitedNetwork.class);
		query.setString(0, identity);
		VisitedNetwork result = null;
		
		try{
			result = (VisitedNetwork) query.uniqueResult();
		}
		catch(org.hibernate.NonUniqueResultException e){
			logger.error("Query did not returned an unique result! You have a duplicate in the database!");
			e.printStackTrace();
		}
		
		return result;
	}

	public static Object[] get_by_Wildcarded_Identity(Session session, String identity, 
			int firstResult, int maxResults){
		
		Query query;
		query = session.createSQLQuery("select * from visited_network where identity like ?")
			.addEntity(VisitedNetwork.class);
		query.setString(0, "%" + identity + "%");

		Object[] result = new Object[2];
		result[0] = new Integer(query.list().size());
		query.setFirstResult(firstResult);
		query.setMaxResults(maxResults);
		result[1] = query.list();
		return result;
	}		
	
	public static Object[] get_all(Session session, int firstResult, int maxResults){
		Query query;
		query = session.createSQLQuery("select * from visited_network")
			.addEntity(VisitedNetwork.class);
		
		Object[] result = new Object[2];
		result[0] = new Integer(query.list().size());
		query.setFirstResult(firstResult);
		query.setMaxResults(maxResults);
		result[1] = query.list();
		return result;
	}

	public static List get_all(Session session){
		Query query;
		query = session.createSQLQuery("select * from visited_network")
			.addEntity(VisitedNetwork.class);
		
		return query.list();
	}
	
	public static int delete_by_ID(Session session, int id){
		Query query = session.createSQLQuery("delete from visited_network where id=?");
		query.setInteger(0, id);
		return query.executeUpdate();
	}
}
