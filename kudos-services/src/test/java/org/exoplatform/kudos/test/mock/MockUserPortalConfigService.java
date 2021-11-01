package org.exoplatform.kudos.test.mock;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.config.UserPortalConfigService;

public class MockUserPortalConfigService extends UserPortalConfigService {

  public MockUserPortalConfigService() throws Exception {
    super(null, null, null, null, null, null, null, new InitParams());
  }

  @Override
  public String getDefaultPortal() {
    return "dw";
  }

}
