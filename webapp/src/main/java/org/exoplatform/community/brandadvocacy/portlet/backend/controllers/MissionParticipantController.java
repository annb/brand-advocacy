package org.exoplatform.community.brandadvocacy.portlet.backend.controllers;

import juzu.Path;
import juzu.Response;
import juzu.SessionScoped;
import juzu.View;
import juzu.request.SecurityContext;
import org.exoplatform.brandadvocacy.model.*;
import org.exoplatform.brandadvocacy.service.IService;
import org.exoplatform.brandadvocacy.service.Utils;
import org.exoplatform.community.brandadvocacy.portlet.backend.JuZBackEndApplication_;
import org.exoplatform.community.brandadvocacy.portlet.backend.models.MissionParticipantDTO;
import org.exoplatform.community.brandadvocacy.portlet.backend.models.ParticipantDTO;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by exoplatform on 10/12/14.
 */
@SessionScoped
public class MissionParticipantController {

  OrganizationService organizationService;
  IdentityManager identityManager;
  IService missionParticipantService;

  String currentProgramId;
  String currentMissionParticipantId;
  @Inject
  LoginController loginController;
  @Inject
  @Path("mission_participant/list.gtmpl")
  org.exoplatform.community.brandadvocacy.portlet.backend.templates.mission_participant.list listTpl;

  @Inject
  @Path("mission_participant/view.gtmpl")
  org.exoplatform.community.brandadvocacy.portlet.backend.templates.mission_participant.view viewTpl;


  @Inject
  public MissionParticipantController(OrganizationService organizationService,IdentityManager identityManager ,IService iService){
    this.organizationService = organizationService;
    this.identityManager = identityManager;
    this.missionParticipantService = iService;
    this.currentProgramId = null;
    this.currentMissionParticipantId = null;
  }

  public Response index(SecurityContext securityContext,String action,String programId){

    loginController.setCurrentUserName(securityContext.getUserPrincipal().getName());
    this.currentProgramId = programId;
    if (null != programId && !"".equals(programId)){
      return this.list();
    }
    return JuZBackEndApplication_.index("cannot manage mission participant in program null");

  }

  public Response index(SecurityContext securityContext,String action,String programId,String missionParticipantId){

    loginController.setCurrentUserName(securityContext.getUserPrincipal().getName());
    this.currentProgramId = programId;
    this.currentMissionParticipantId = missionParticipantId;
    if (null != missionParticipantId && !"".equals(missionParticipantId)){
      return this.view();
    }
    return JuZBackEndApplication_.index("cannot manage mission participant in program null");

  }
  public Response list(){

    List<MissionParticipantDTO> missionParticipantDTOs = new ArrayList<MissionParticipantDTO>();
    MissionParticipantDTO missionParticipantDTO;
    Mission mission;
    List<MissionParticipant>  missionParticipants = this.missionParticipantService.getAllMissionParticipantsInProgram(this.currentProgramId);
    User exoUser;
    for (MissionParticipant missionParticipant : missionParticipants){
      try {
        exoUser = this.organizationService.getUserHandler().findUserByName(missionParticipant.getParticipant_username());
        if(null != exoUser){
          mission = this.missionParticipantService.getMissionById(missionParticipant.getMission_id());
          if (null != mission){
            missionParticipantDTO = new MissionParticipantDTO();
            missionParticipantDTO.setId(missionParticipant.getId());
            missionParticipantDTO.setMission_title(mission.getTitle());
            missionParticipantDTO.setParticipant_fullName(exoUser.getFirstName()+ " "+exoUser.getLastName());
            missionParticipantDTO.setStatus(missionParticipant.getStatus());
            missionParticipantDTO.setUrl_submitted(missionParticipant.getUrl_submitted());
            missionParticipantDTO.setDate_submitted(Utils.convertDateFromLong(missionParticipant.getModifiedDate()));
            missionParticipantDTOs.add(missionParticipantDTO);
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return listTpl.with().set("missionParticipantDTOs",missionParticipantDTOs).set("states", Status.values()).ok();
  }

  public Response view(){
    MissionParticipant missionParticipant = this.missionParticipantService.getMissionParticipantById(this.currentMissionParticipantId);
    if(null != missionParticipant){
      try {
        Mission mission = this.missionParticipantService.getMissionById(missionParticipant.getMission_id());
        if(null != mission){
          Identity identity = this.identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME,missionParticipant.getParticipant_username(),true);
          if (null != identity){
            MissionParticipantDTO missionParticipantDTO = new MissionParticipantDTO();
            missionParticipantDTO.setMission_title(mission.getTitle()+" on "+mission.getThird_part_link());
            missionParticipantDTO.setSize(missionParticipant.getSize().getLabel());
            missionParticipantDTO.setDate_submitted(Utils.convertDateFromLong(missionParticipant.getModifiedDate()));
            missionParticipantDTO.setStatus(missionParticipant.getStatus());
            missionParticipantDTO.setUrl_submitted(missionParticipant.getUrl_submitted());

            ParticipantDTO participantDTO = new ParticipantDTO();
            participantDTO.setFullName(identity.getProfile().getFullName());
            participantDTO.setUrlAvatar(identity.getProfile().getAvatarUrl());
            participantDTO.setUrlProfile(identity.getProfile().getUrl());
            participantDTO.setEmail(identity.getProfile().getEmail());
            Address address = this.missionParticipantService.getAddressById(missionParticipant.getAddress_id());
            return viewTpl.with().set("missionParticipantDTO",missionParticipantDTO).set("address",address).set("participantDTO",participantDTO).set("states",Status.values()).ok();

          }
        }

      } catch (Exception e) {
        return Response.ok("nok");
      }
    }
    return Response.ok("nok");
  }


}
