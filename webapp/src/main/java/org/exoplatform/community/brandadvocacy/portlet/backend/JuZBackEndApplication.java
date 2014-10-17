package org.exoplatform.community.brandadvocacy.portlet.backend;

import juzu.*;
import juzu.request.SecurityContext;
import juzu.template.Template;
import org.exoplatform.brandadvocacy.service.IService;
import org.exoplatform.community.brandadvocacy.portlet.backend.controllers.LoginController;
import org.exoplatform.community.brandadvocacy.portlet.backend.controllers.MissionController;
import org.exoplatform.community.brandadvocacy.portlet.backend.templates.index;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.manager.IdentityManager;

import javax.inject.Inject;

/**
 * Created by exoplatform on 01/10/14.
 */
@SessionScoped
public class JuZBackEndApplication {

  @Inject
  @Path("index.gtmpl")
  org.exoplatform.community.brandadvocacy.portlet.backend.templates.index index;

  OrganizationService organizationService;
  IService brandAdvocacyService;

  @Inject
  MissionController missionController;
  @Inject
  LoginController loginController;

  @Inject
  public JuZBackEndApplication(OrganizationService organizationService,IService brandAdvocacyService){
    this.organizationService = organizationService;
    this.brandAdvocacyService = brandAdvocacyService;
  }

  @View
  public Response.Content index(){
    return index.with().set("remoteUserName",loginController.getCurrentUserName()).ok();
  }

}
