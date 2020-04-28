/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.kudos.test;

import org.junit.*;

import org.exoplatform.container.RootContainer;
import org.exoplatform.social.service.test.AbstractResourceTest;

public abstract class BaseKudosRestTest extends BaseKudosTest {

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
