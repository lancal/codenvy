<%--

    CODENVY CONFIDENTIAL
    __________________

     [2012] - [2015] Codenvy, S.A.
     All Rights Reserved.

    NOTICE:  All information contained herein is, and remains
    the property of Codenvy S.A. and its suppliers,
    if any.  The intellectual and technical concepts contained
    herein are proprietary to Codenvy S.A.
    and its suppliers and may be covered by U.S. and Foreign Patents,
    patents in process, and are protected by trade secret or copyright law.
    Dissemination of this information or reproduction of this material
    is strictly forbidden unless prior written permission is obtained
    from Codenvy S.A..

--%>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Session View</title>
    <%@ include file="/inclusions/header.jsp"%>
</head>
<body>

<jsp:include page="/inclusions/top-menu/top-menu.jsp">
    <jsp:param name="selectedMenuItemId" value="topmenu-sessions"/>
</jsp:include>

<div class="container-fluid">
    <div class="row-fluid">
        <div>
            <div class="well topFilteringPanel">
                <div id="filter-by" class="left" targetWidgets="userSessionActivity">
                    <div class="collabsiblePanelTitle">Filter</div>
                    <div class="collabsiblePanelBody">
                        <table>
                            <tr>
                               <td><label for="input-action">Event:</label></td>
                               <td>
                                   <div class="filter-item">
                                       <input type="text" id="input-action" name="action" class="text-box"/>
                                   </div>
                               </td>
                            </tr>
                            <tr>
                               <td><label for="input-event_parameter_name">Parameter Name:</label></td>
                               <td>
                                   <div class="event_parameter_name">
                                       <input type="text" id="input-event_parameter_name" name="event_parameter_name" class="text-box"/>
                                   </div>
                               </td>
                            </tr>
                            <tr>
                               <td><label for="input-event_parameter_value">Parameter Value:</label></td>
                               <td>
                                   <div class="filter-item">
                                       <input type="text" id="input-event_parameter_value" name="event_parameter_value" class="text-box"/>
                                   </div>
                               </td>
                            </tr>
                            <tr>
                               <td></td>
                               <td><div class="filter-item">
                                    <button class="btn command-btn btn-primary">Filter</button>                    
                                    <button id="clearSelectionBtn" class="btn btn-small clear-btn">Clear</button>
                               </div></td>
                            </tr>
                        </table>
                    </div>
                </div>
            </div>
        
            <div class="hero-unit">
                <div class='view'>
                    <div class='overview'>
                       <div id="sessionOverview"></div>
                    </div>
                </div>

                <div class="single-column-gadget full-width">
                    <div class="view">
                        <div class="tables">
                            <div class="item" id="userSessionActivity"></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/inclusions/footer.jsp">
    <jsp:param name="javaScriptToLoad" value="/analytics/scripts/presenters/VerticalTablePresenter.js"/>
    <jsp:param name="javaScriptToLoad" value="/analytics/scripts/presenters/HorizontalTablePresenter.js"/>
</jsp:include>

</body>
</html>