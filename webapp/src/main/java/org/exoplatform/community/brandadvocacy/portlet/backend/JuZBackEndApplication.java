package org.exoplatform.community.brandadvocacy.portlet.backend;

import juzu.*;
import juzu.request.SecurityContext;
import juzu.template.Template;
import org.exoplatform.brandadvocacy.model.Manager;
import org.exoplatform.brandadvocacy.model.Mission;
import org.exoplatform.brandadvocacy.model.Program;
import org.exoplatform.brandadvocacy.service.IService;
import org.exoplatform.community.brandadvocacy.portlet.backend.controllers.*;
import org.exoplatform.community.brandadvocacy.portlet.backend.templates.index;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.manager.IdentityManager;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by exoplatform on 01/10/14.
 */
public class JuZBackEndApplication {

  @Inject
  @Path("index.gtmpl")
  org.exoplatform.community.brandadvocacy.portlet.backend.templates.index indexTpl;

  OrganizationService organizationService;
  IService jcrService;

  @Inject
  MissionController missionController;
  @Inject
  LoginController loginController;
  @Inject
  ProgramController programController;
  @Inject
  PropositionController propositionController;

  @Inject
  MissionParticipantController missionParticipantController;

  @Inject
  public JuZBackEndApplication(OrganizationService organizationService,IService iService){
    this.organizationService = organizationService;
    this.jcrService = iService;
  }

  @View
  public Response index(SecurityContext securityContext,String action) {

    if (null == loginController.getCurrentProgramId()){
      List<Program> programs = this.jcrService.getAllPrograms();
      Program program = null;
      if (programs.size() > 0){
        program = programs.get(0);
        loginController.setCurrentProgramId(program.getId());
        loginController.setCurrentUserName(securityContext.getUserPrincipal().getName());
        this.getRights();
      }
    }
    if (null != action){
      if (action.equals("mission_participant_index"))
        return missionParticipantController.index();
      else if (action.equals("mission_index"))
        return missionController.index();
    }
    if (loginController.isAdmin()){
      return programController.index();
    }else{
      return missionParticipantController.index();
    }

  }
  @View
  public Response showError(String msg){
    return indexTpl.with().set("msg",msg).ok();
  }

  private void getRights(){
    Manager manager = this.jcrService.getProgramManagerByUserName(loginController.getCurrentProgramId(),loginController.getCurrentUserName());
    if (null != manager){
      loginController.setRights(manager.getRole().getLabel());
    }
  }
}
