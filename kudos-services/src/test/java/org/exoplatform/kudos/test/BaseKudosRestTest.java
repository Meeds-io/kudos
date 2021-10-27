package org.exoplatform.kudos.test;

import org.junit.*;

import org.exoplatform.container.RootContainer;
import org.exoplatform.social.service.test.AbstractResourceTest;

public abstract class BaseKudosRestTest extends BaseKudosTest {

  protected static final String      DEFAULT_PORTAL = "meeds";

  protected static KudosResourceTest resourceTest = new KudosResourceTest();

  @BeforeClass
  public static void beforeTest() {
    BaseKudosTest.beforeTest();
    resourceTest.begin();
  }

  @Before
  public void beforeBeginTest() throws Exception {
    super.beforeMethodTest();
    resourceTest.setUp();
  }

  @After
  public void afterEndTest() throws Exception {
    super.beforeMethodTest();
    resourceTest.tearDown();
  }

  public static class KudosResourceTest extends AbstractResourceTest {

    public KudosResourceTest() {
      setForceContainerReload(true);
    }

    @Override
    public void begin() { // NOSONAR
      super.begin();
    }

    @Override
    public void setUp() throws Exception { // NOSONAR
      super.setUp();
      // Workaround for PortalContainer is not registered n RootContainer
      // That caused 'Session is closed' on Hibernate
      RootContainer.getInstance().getPortalContainer("portal");
    }

    @Override
    public void tearDown() throws Exception { // NOSONAR
      super.tearDown();
      super.end();
    }

    @Override
    public void startSessionAs(String user) { // NOSONAR
      super.startSessionAs(user);
    }

    @Override
    public String getURLResource(String resourceURL) {
      return "/kudos/api/kudos/" + resourceURL;
    }

    @Override
    protected void deleteAllRelationships() throws Exception {
      // Nop
    }

    @Override
    protected void deleteAllIdentitiesWithActivities() throws Exception {
      // Nop
    }

    @Override
    protected void deleteAllSpaces() throws Exception {
      // Nop
    }
  }
}
