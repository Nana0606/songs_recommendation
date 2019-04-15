(function ($) {
    var kendo = window.kendo;

    var ExcelGrid = kendo.ui.Grid.extend({
        init: function (element, options) {
            var that = this;

            if (options.export) {
                // If the exportCssClass is not defined, then set a default image.
                options.export.cssClass = "k-grid-export-image";

                // Add the export toolbar button.
                options.toolbar = $.merge([
                    {
                        name: "export",
                        template: kendo.format("<a class='k-button k-button-icontext k-grid-export' title='ייצא לקובץ Excel'><div class='{0} k-icon'></div>קובץ Excel</a>", options.export.cssClass)
                    }
                ], options.toolbar || []);
            }

            // Initialize the grid.
            kendo.ui.Grid.fn.init.call(that, element, options);

            // Add an event handler for the Export button.
            $(element).on("click", ".k-grid-export", { sender: that }, function (e) {
                e.data.sender.exportToExcel();
            });
        },

        options: {
            name: "ExcelGrid"
        },

        exportToExcel: function () {
            var that = this;

            $.ajax({
                url: getBaseURL() + "Admin/Home/ExportToExcel",
                data: {
                    model: JSON.stringify(that.columns),
                    data: JSON.stringify(that.dataSource.data().toJSON()),
                    title: that.options.export.title
                },
                type: 'POST',
                success: function () {
                    window.location.href = getBaseURL() + "Admin/Home/GetExcelFile?title=" + encodeURIComponent(that.options.export.title);
                },
                error: function () {
                    okAlertDialog('הפעולה לא בוצעה בהצלחה');
                }
            });
        }
    });

    kendo.ui.plugin(ExcelGrid);
})(jQuery);