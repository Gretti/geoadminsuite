/*
* 
*  The JUMP DB Query Plugin is Copyright (C) 2007  Larry Reeder
*  JUMP is Copyright (C) 2003 Vivid Solutions
* 
*  This file is part of the JUMP DB Query Plugin.
*  
*  The JUMP DB Query Plugin is free software; you can redistribute it and/or 
*  modify it under the terms of the Lesser GNU General Public License as 
*  published *  by the Free Software Foundation; either version 3 of the 
*  License, or  (at your option) any later version.
*  
*  This software is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  Lesser GNU General Public License for more details.
*  
*  You should have received a copy of the GNU General Public License
*  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package unit.org.freevoice.jumpdbqueryextension.ui;


import junit.framework.TestCase;
import org.freevoice.jumpdbqueryextension.ui.QueryDialog;
import org.freevoice.jumpdbqueryextension.util.DbConnectionParameters;

import javax.swing.*;
import java.util.List;
import java.util.ArrayList;
import java.awt.event.ActionEvent;

public class TestQueryDialog extends TestCase
{

    public static void main(String[] args) throws Exception
    {
        junit.textui.TestRunner.run(TestQueryDialog.class);
    }

    public void setUp() throws Exception
    {
    }

    public void tearDown() throws Exception
    {
    }

    public void testQueryDialog()
    {
        DbConnectionParameters dbConnectionParameters =
                new DbConnectionParameters("displayName", "className",
                        "driverClass", "jdbcUrl", "username", "password");

        List<DbConnectionParameters> connectionParamsList = new ArrayList<DbConnectionParameters>();

        QueryDialog queryDialog = new QueryDialog(null, new JFrame(), "test", connectionParamsList, false);
        // Nico: added now component construction is made in this method
        queryDialog.initUICode();

        DbConnectionParameters[] paramArray = new DbConnectionParameters[1];
        paramArray[0] = dbConnectionParameters;
        JComboBox comboBox = new JComboBox(paramArray);

        comboBox.setSelectedItem(dbConnectionParameters);

        int runId = 1;
        ActionEvent runCommandEvent =  new ActionEvent(comboBox, runId, "DbChanged");

        queryDialog.actionPerformed(runCommandEvent);

        assertEquals("Class name should be \"className\", was  " + queryDialog.getQueryClass(),
                "className", queryDialog.getQueryClass());

    }
}
