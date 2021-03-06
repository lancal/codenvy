/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
if (typeof analytics === "undefined") {
    analytics = {};
}

analytics.presenter = analytics.presenter || {};

analytics.presenter.HorizontalTablePresenter = function HorizontalTablePresenter() {
};

analytics.presenter.HorizontalTablePresenter.prototype = new Presenter();

analytics.presenter.HorizontalTablePresenter.prototype.DEFAULT_ONE_PAGE_ROWS_COUNT = 10;

analytics.presenter.HorizontalTablePresenter.prototype.load = function () {
    var presenter = this;
    var model = presenter.model;
    var widgetName = presenter.widgetName;
    
    var view = presenter.view;
    var viewParams = view.getParams();
    var modelParams = presenter.getModelParams(viewParams);
    model.setParams(modelParams);
    
    presenter.displayEmptyWidget();
    
    var isPaginable = analytics.configuration.getProperty(widgetName, "isPaginable", false);   // default value is "false"

    //process pagination
    if (isPaginable) {
        var onePageRowsCount = analytics.configuration.getProperty(widgetName, "onePageRowsCount", presenter.DEFAULT_ONE_PAGE_ROWS_COUNT);
        var currentPageNumber = viewParams[widgetName] || 1;  // search on table page number in parameter "{modelViewName}={page_number}"            
        modelParams.page = currentPageNumber;
        modelParams.per_page = onePageRowsCount;

        model.setParams(modelParams);
        model.pushDoneFunction(function (tables) {
            var modelParams = presenter.getModelParams(viewParams);  // restore initial model params

            var table = tables[0];  // there is only one table in tables
            table.original = analytics.util.clone(table, true, []);

            // add links to drill down page
            table = presenter.linkTableValuesWithDrillDownPage(presenter.widgetName, table, modelParams);

            // make table columns linked
            var columnCombinedLinkConf = analytics.configuration.getProperty(presenter.widgetName, "columnCombinedLinkConfiguration");
            if (typeof columnCombinedLinkConf != "undefined") {
                table = presenter.makeTableColumnCombinedLinked(table, columnCombinedLinkConf);    
            }

            var columnLinkPrefixList = analytics.configuration.getProperty(widgetName, "columnLinkPrefixList");
            if (typeof columnLinkPrefixList != "undefined") {
                for (var columnName in columnLinkPrefixList) {
                    table = presenter.makeTableColumnLinked(table, columnName, columnLinkPrefixList[columnName]);
                }
            }

            var pageCount = model.recognizePageCount(onePageRowsCount, currentPageNumber, table.rows.length);
            
            modelParams[widgetName] = modelParams.page;
            delete modelParams.page;    // remove page parameter
            delete modelParams.per_page;    // remove page parameter

            if (pageCount != 1) {
                // make table header as linked for sorting
                var mapColumnToServerSortParam = analytics.configuration.getProperty(widgetName, "mapColumnToServerSortParam", undefined);
                table = presenter.addServerSortingLinks(table, widgetName, modelParams, mapColumnToServerSortParam);

                // print table
                presenter.printTable(table);

                // print bottom page navigation
                view.printBottomPageNavigator(pageCount, currentPageNumber, modelParams, widgetName, widgetName);

                view.loadTableHandlers(false);  // don't display client side sorting for table with pagination
            } else {
                presenter.printTable(table, widgetName + "_table");

                // display client sorting
                var clientSortParams = analytics.configuration.getProperty(widgetName, "clientSortParams");
                view.loadTableHandlers(true, clientSortParams, widgetName + "_table");
            }

            // finish loading widget
            presenter.needLoader = false;
        });

        var modelViewName = analytics.configuration.getProperty(widgetName, "modelViewName");
        model.getModelViewData(modelViewName);

    } else {
        model.setParams(modelParams);

        model.pushDoneFunction(function (tables) {
            // print table
            var table = tables[0];  // there is only one table in tables
            table.original = analytics.util.clone(table, true, []);

            // add links to drill down page
            table = presenter.linkTableValuesWithDrillDownPage(presenter.widgetName, table, modelParams);

            // make table columns linked
            var columnCombinedLinkConf = analytics.configuration.getProperty(presenter.widgetName, "columnCombinedLinkConfiguration");
            if (typeof columnCombinedLinkConf != "undefined") {
                table = presenter.makeTableColumnCombinedLinked(table, columnCombinedLinkConf);    
            }

            var columnLinkPrefixList = analytics.configuration.getProperty(widgetName, "columnLinkPrefixList");
            if (typeof columnLinkPrefixList != "undefined") {
                for (var columnName in columnLinkPrefixList) {
                    table = presenter.makeTableColumnLinked(table, columnName, columnLinkPrefixList[columnName]);
                }
            }

            presenter.printTable(table, widgetName + "_table");

            // display client sorting
            var clientSortParams = analytics.configuration.getProperty(widgetName, "clientSortParams");
            view.loadTableHandlers(true, clientSortParams, widgetName + "_table");

            // finish loading widget
            presenter.needLoader = false;
        });

        var modelViewName = analytics.configuration.getProperty(widgetName, "modelViewName");
        model.getModelViewData(modelViewName);
    }
};

analytics.presenter.HorizontalTablePresenter.prototype.printTable = function (table, tableId) {
    var view = this.view;

    view.print("<div class='body'>");
    
    // print table
    if (table.rows.length == 1) {
        view.printTable(table, false, tableId, "text-aligned-center"); // align text by center if there is 1 row in table
    } else {
        view.printTable(table, false, tableId);
    }

    view.print("</div>");
}
