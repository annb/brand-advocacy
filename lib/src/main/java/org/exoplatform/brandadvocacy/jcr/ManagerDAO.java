/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.brandadvocacy.jcr;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.exoplatform.brandadvocacy.model.Manager;
import org.exoplatform.brandadvocacy.model.Mission;
import org.exoplatform.brandadvocacy.model.Role;
import org.exoplatform.brandadvocacy.service.BrandAdvocacyServiceException;
import org.exoplatform.brandadvocacy.service.JCRImpl;
import org.exoplatform.brandadvocacy.service.Utils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Sep 18, 2014  
 */
public class ManagerDAO extends DAO{

  public static final String node_prop_mission_id = "exo:mission_id";
  public static final String node_prop_username = "exo:username";
  public static final String node_prop_role = "exo:role";
  public static final String node_prop_notif = "exo:notification";
  private static final Log log = ExoLogger.getLogger(ManagerDAO.class);
  public ManagerDAO(JCRImpl jcrImpl) {
    super(jcrImpl);
  }
  public void setProperties(Node aNode,Manager m) throws RepositoryException{
    aNode.setProperty(node_prop_mission_id,m.getMission_id());
    aNode.setProperty(node_prop_username,m.getUserName());
    aNode.setProperty(node_prop_notif, m.getNotif());
    aNode.setProperty(node_prop_role, m.getRole().getValue());
  }
  public Manager transferNode2Object(Node node) throws RepositoryException{
    Manager manager = new Manager();
    PropertyIterator iter = node.getProperties("exo:*");
    while (iter.hasNext()) {
      Property p = (Property) iter.next(); 
      String name = p.getName();
      if(name.equals(node_prop_mission_id)){
        manager.setMission_id(p.getString());
      }
      else if(name.equals(node_prop_username)){
        manager.setUserName(p.getString());
      }
      else if(name.equals(node_prop_role)){
        manager.setRole(Role.getRole((int)p.getLong()));
      }
      else if(name.equals(node_prop_notif)){
        manager.setNotif(p.getBoolean());
      }
    }
    return manager;
  }
  public Node getManagerNode(String missionLabelId,String username){
    StringBuilder sql = new StringBuilder("select * from "+ JCRImpl.MANAGER_NODE_TYPE +" where jcr:path like '");
    sql.append(JCRImpl.EXTENSION_PATH).append("/").append(JCRImpl.MISSIONS_PATH);
    sql.append("/").append(Utils.queryEscape(missionLabelId)).append("/").append(MissionDAO.node_prop_managers);
    sql.append("/").append(Utils.queryEscape(username));
    sql.append("'");
    Session session;
    try {
      session = this.getJcrImplService().getSession();
      Query query = session.getWorkspace().getQueryManager().createQuery(sql.toString(), Query.SQL);
      QueryResult result = query.execute();
      NodeIterator nodes = result.getNodes();
      if (nodes.hasNext()) {
        return nodes.nextNode();
      }
    } catch (RepositoryException e) {
      log.error("ERROR cannot get manager  "+username +" from mission "+missionLabelId+" Exeption "+e.getMessage());
    }
    return null;
  }
  public Mission addManager2Mission(String mid,List<Manager> managers){
    try {
      if(null == mid || "".equals(mid))
        throw new BrandAdvocacyServiceException(BrandAdvocacyServiceException.ID_INVALID, "cannot add managers to mission id null");

      try {
        Node missionNode = this.getJcrImplService().getMissionDAO().getNodeById(mid);
        Node managerHomeNode = this.getJcrImplService().getMissionDAO().getOrCreateManagerHome(missionNode);
        if(null != managerHomeNode){
          int nb = (int) managerHomeNode.getNodes().getSize();
          Manager manager;
          Node managerNode;
          int i = 0;
          for(i = 0;i<managers.size();i++){
            manager = managers.get(i);
            manager.setMission_id(mid);
            if (!managerHomeNode.hasNode(manager.getUserName())){
              managerNode = managerHomeNode.addNode(manager.getUserName(), JCRImpl.MANAGER_NODE_TYPE);
              this.setProperties(managerNode, manager);
            }

          }
          if(0 != i) {
            managerHomeNode.getSession().save();
            return this.getJcrImplService().getMissionDAO().transferNode2Object(missionNode);
          }
        }
      } catch (ItemExistsException ie){
        log.error(" === ERROR cannot add existing item "+ie.getMessage());
      } catch (RepositoryException e) {
        log.error("=== ERROR add manager to mission "+e.getMessage());
      }


    } catch (BrandAdvocacyServiceException brade) {
      log.error("ERROR cannot add manager "+brade.getMessage());
    }
    return null;
  }  
  public List<Manager> getAllManagers(String mid){

    List<Manager> managers = new ArrayList<Manager>();

    if(null == mid || "".equals(mid))
      throw new BrandAdvocacyServiceException(BrandAdvocacyServiceException.ID_INVALID, "cannot get all managers from mission id null");
    try {
      Node missionNode = this.getJcrImplService().getMissionDAO().getNodeById(mid);
      if(null != missionNode){
        Node managerHomeNode = this.getJcrImplService().getMissionDAO().getOrCreateManagerHome(missionNode);
        NodeIterator nodes = managerHomeNode.getNodes();
        Node manager = null;
        Manager aManager = null;
        while (nodes.hasNext()) {
          manager = (Node) nodes.next();
          aManager = this.transferNode2Object(manager);
          if(aManager.checkValid())
            managers.add(aManager);
        }
        return managers;
      }
    } catch (RepositoryException e) {
        log.error("==== ERROR get all managers "+e.getMessage() );
    }
    return managers;
  }
  public Manager updateManager(Manager manager){
    if(!manager.checkValid())
      return null;
    try {
      Node managerNode = this.getManagerNode(manager.getMissionLabelId(),manager.getUserName());
      if(null != managerNode && manager.getUserName().equals(managerNode.getProperty(node_prop_username).getString())){
        this.setProperties(managerNode,manager);
        managerNode.save();
        return this.transferNode2Object(managerNode);
      }else
        throw new BrandAdvocacyServiceException(BrandAdvocacyServiceException.MANAGER_NOT_EXISTS," cannot update manager not exists");
    } catch (RepositoryException e) {
      log.error("==== ERROR cannot update manager "+e.getMessage() );
    } catch (BrandAdvocacyServiceException brade){
      log.error(brade.getMessage());
    }
    return null;
  }
  public void removeManager(String missionLabelId,String username){
    try {
      Node managerNode = this.getManagerNode(missionLabelId,username);
      if(null != managerNode){
        Session session = managerNode.getSession();
        managerNode.remove();
        session.save();
      }else
        throw new BrandAdvocacyServiceException(BrandAdvocacyServiceException.MANAGER_NOT_EXISTS," cannot remove manager not exists");
    } catch (RepositoryException e) {
      log.error("==== ERROR cannot remove manager "+e.getMessage() );
    } catch (BrandAdvocacyServiceException brade){
      log.error(brade.getMessage());
    }

  }
  public Manager getManager(String missionLabelId,String username){
    if(null == username || null == missionLabelId || "".equals(missionLabelId) || "".equals(username))
      return null;
    try {
      return this.transferNode2Object(this.getManagerNode(missionLabelId,username));
    } catch (RepositoryException e) {
      log.error(" ERROR cannot get manager from mission");
    }

    return null;

  }
}
