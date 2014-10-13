/**
 * Created by exoplatform on 07/10/14.
 */
@Application(defaultController = JuZFrontEndApplication.class)
@WebJars(@WebJar("jquery"))
@Portlet(name="FrontendPortlet")
@Bindings(
        {
                @Binding(value = org.exoplatform.services.organization.OrganizationService.class),
                @Binding(value = org.exoplatform.brandadvocacy.service.IService.class)
        }
)

@Scripts({
        @Script(id = "jquery", value = "jquery/1.10.2/jquery.js"),
        @Script(
                id = "bradFTJS", value = "js/brad-frontend.js",location = AssetLocation.SERVER,depends = {"jquery"}
        )
})

@Assets("*")

package org.exoplatform.community.brandadvocacy.portlet.frontend;

import juzu.Application;
import juzu.asset.AssetLocation;
import juzu.plugin.asset.Assets;
import juzu.plugin.asset.Script;
import juzu.plugin.asset.Scripts;
import juzu.plugin.binding.Binding;
import juzu.plugin.binding.Bindings;
import juzu.plugin.portlet.Portlet;
import juzu.plugin.webjars.WebJar;
import juzu.plugin.webjars.WebJars;;