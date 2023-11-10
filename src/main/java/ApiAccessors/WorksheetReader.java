package ApiAccessors;

import GraphComponents.GraphComponent;
import com.microsoft.graph.models.*;
import com.microsoft.graph.requests.*;

import java.io.IOException;

import static ApiAccessors.JsonHelper.*;

public class WorksheetReader {
    GraphServiceClient<Request> graphClient;
    DriveItemRequestBuilder driveItem;
    private GraphComponent[] graphComponents;

    public WorksheetReader(GraphServiceClient client, DriveItemRequestBuilder driveItem) {
        this.graphClient = client;
        this.driveItem = driveItem;
    }

    public GraphComponent[] GetData() {
        return graphComponents;
    }

    public void ReadSheet() throws IOException {
        WorkbookWorksheetCollectionPage worksheets = driveItem.workbook().worksheets().buildRequest().get();

        if (worksheets == null)
            return;
        for (WorkbookWorksheet sheet : worksheets.getCurrentPage()) {
            WorkbookRange cell = driveItem.workbook().worksheets(sheet.id)
                    .cell(WorkbookWorksheetCellParameterSet.newBuilder().withColumn(1).withRow(1).build())
                    .buildRequest()
                    .get();
            System.out.println("Cells: " + cell.text.getAsString());

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
