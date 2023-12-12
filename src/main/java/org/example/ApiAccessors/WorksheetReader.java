package org.example.ApiAccessors;

import com.microsoft.graph.models.WorkbookRange;
import com.microsoft.graph.models.WorkbookWorksheet;
import com.microsoft.graph.requests.DriveItemRequestBuilder;
import com.microsoft.graph.requests.DriveRequestBuilder;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.WorkbookWorksheetCollectionPage;
import okhttp3.Request;
import org.example.GraphComponents.GraphComponent;

import java.io.IOException;

import static org.example.ApiAccessors.JsonHelper.ReadJsonSheet;

public class WorksheetReader {
    DriveItemRequestBuilder driveItem;
    private GraphComponent[] graphComponents;

    public WorksheetReader(GraphServiceClient<Request> client, DriveRequestBuilder drive, String sheetId, boolean isShared) {
        if (!isShared) {
            driveItem = drive.items(sheetId);
            System.out.println(driveItem.getRequestUrl());
        }
        else {
            // Unbelievably convoluted way to get shared items
            for (var i : drive.sharedWithMe().buildRequest().get().getCurrentPage())
            {
                if (i.id.equals(sheetId)) {
                    driveItem = client.drives(i.remoteItem.parentReference.driveId).items(sheetId);
                    break;
                }
            }
        }
    }

    public GraphComponent[] GetData() {
        return graphComponents;
    }

    public void ReadSheet() throws IOException {
        WorkbookWorksheetCollectionPage worksheets = driveItem.workbook().worksheets().buildRequest().get();

        if (worksheets == null)
            return;
        for (WorkbookWorksheet sheet : worksheets.getCurrentPage()) {
            //WorkbookRange cell = driveItem.workbook().worksheets(sheet.id)
            //        .cell(WorkbookWorksheetCellParameterSet.newBuilder().withColumn(1).withRow(1).build())
            //        .buildRequest()
            //        .get();
            //System.out.println("Cells: " + cell.text.getAsString());

            WorkbookRange range = driveItem.workbook().worksheets(sheet.id)
                    .usedRange()
                    .buildRequest()
                    .get();

            String[][] sheetArray = ReadJsonSheet(range.text);
            graphComponents = new GraphComponent[sheetArray.length];
            for (int i = 0; i < sheetArray.length; i++)
                graphComponents[i] = readComponent(sheetArray[i]);
        }
    }

    private GraphComponent readComponent(String[] componentData) {
        if (componentData.length < 3)
            return null;
        int current = 0;
        int base = 0;
        try {
            current = Integer.valueOf(componentData[1]);
            base = Integer.valueOf(componentData[2]);
        }
        catch (Exception e) {
            return null;
        }

        GraphComponent component = new GraphComponent(componentData[0]);
        component.SetCurrentValue(current);
        component.SetBaseValue(base);
        return component;
    }
}
