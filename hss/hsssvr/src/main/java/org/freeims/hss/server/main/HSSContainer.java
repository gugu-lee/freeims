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

package org.freeims.hss.server.main;

import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.freeims.hss.common.HSSProperties;
import org.freeims.hss.server.db.hibernate.HibernateUtil;
import org.freeims.hss.server.diam.*;
import org.hibernate.Session;

/**
 * @author adp dot fokus dot fraunhofer dot de Adrian Popescu / FOKUS Fraunhofer
 *         Institute
 */

public class HSSContainer {

	private static Logger logger = Logger.getLogger(HSSContainer.class);

	public LinkedBlockingQueue tasksQueue;
	public Worker[] workers;

	// diameter stack
	public DiameterStack diamStack;

	public HSSContainer() {

		// invoke hibernate for the first time
		Session session = HibernateUtil.getCurrentSession();
		HibernateUtil.commitTransaction();
		HibernateUtil.closeSession();

		// initialise the tasksQueue and start the workers
		tasksQueue = new LinkedBlockingQueue();
		workers = new Worker[10];
		for (int i = 0; i < workers.length; i++) {
			workers[i] = new Worker(this);
			workers[i].start();
		}
		// Initialise the diameter stack
		diamStack = new DiameterStack(this);

		// CxEvents Worker
		CxEventsWorker cxEventsWorker = new CxEventsWorker(diamStack, HSSProperties.CX_EVENT_CHECK_INTERVAL);
		cxEventsWorker.start();

		// ShEvents Worker
		ShEventsWorker shEventsWorker = new ShEventsWorker(diamStack, HSSProperties.SH_NOTIF_CHECK_INTERVAL);
		shEventsWorker.start();

	}

}
