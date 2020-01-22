/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.workbench.common.widgets.metadata.client.widget;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.gwtbootstrap3.client.ui.NavTabs;
import org.gwtbootstrap3.client.ui.TabListItem;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(GwtMockitoTestRunner.class)
public class OverviewWidgetViewImplTest {

    @Test
    public void showVersionHistory() {
        final TabListItem modelTabListItem = mock(TabListItem.class);
        doReturn("Model").when(modelTabListItem).getText();
        final TabListItem overviewTabListItem = mock(TabListItem.class);
        doReturn("Overview").when(overviewTabListItem).getText();

        final NavTabs navTabs = mock(NavTabs.class);
        doReturn(2).when(navTabs).getWidgetCount();
        doReturn(modelTabListItem).when(navTabs).getWidget(0);
        doReturn(overviewTabListItem).when(navTabs).getWidget(1);

        final OverviewWidgetViewImpl overviewWidgetView = new OverviewWidgetViewImpl();
        overviewWidgetView.navTabs = navTabs;

        overviewWidgetView.showVersionHistory();

        verify(overviewTabListItem).showTab();
        verify(modelTabListItem, never()).showTab();
    }
}