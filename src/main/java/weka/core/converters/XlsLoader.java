/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package weka.core.converters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 * <!-- globalinfo-start --> Reads a source that is in .xls (or .xlsx) format.
 * <!-- globalinfo-end -->
 */
public class XlsLoader extends AbstractFileLoader implements BatchConverter {

    public static String FILE_EXTENSION_1 = ".xls";
    public static String FILE_EXTENSION_2 = ".xlsx";

    protected transient FileInputStream m_sourceReader = null;
    protected transient XlsReader m_XlsReader = null;
    protected transient boolean m_isXlsx = true;

    @Override
    public void setFile(File file) throws FileNotFoundException {
        m_sourceReader = new FileInputStream(file);
        if (file.getName().endsWith(".xls")) {
            m_isXlsx = false;
        } else if (file.getName().endsWith(".xlsx")) {
            m_isXlsx = true;
        } else {
            throw new IllegalArgumentException("Wrong extension of filename: " + file);
        }
    }

    @Override
    public Instances getStructure() throws IOException {
        if (m_structure == null) {
            if (m_sourceReader == null) {
                throw new IOException("No source has been specified");
            }

            try {
                m_XlsReader
                        = new XlsReader(m_sourceReader, 1, (getRetrieval() == BATCH), m_isXlsx);
                //m_XlsReader.setRetainStringValues(getRetainStringVals());
                m_structure = m_XlsReader.getStructure();
            } catch (Exception ex) {
                throw new IOException("Unable to determine structure (Reason: "
                        + ex.toString() + ").");
            }
        }

        return new Instances(m_structure, 0);
    }

    @Override
    public Instances getDataSet() throws IOException {
        Instances insts = null;
        try {
            if (m_sourceReader == null) {
                throw new IOException("No source has been specified");
            }
            setRetrieval(BATCH);
            if (m_structure == null) {
                getStructure();
            }

            // Read all instances
            Instance inst;
            while ((inst = m_XlsReader.getInstance(m_XlsReader.m_Data)) != null) {
                m_XlsReader.m_Data.add(inst);
            }
            return m_XlsReader.m_Data;

            // Instances readIn = new Instances(m_structure);
        } finally {
            if (m_sourceReader != null) {
                // close the stream
                m_sourceReader.close();
            }
        }
    }

    @Override
    public Instance getNextInstance(Instances structure) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getRevision() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getFileExtension() {
        return FILE_EXTENSION_1;
    }

    @Override
    public String[] getFileExtensions() {
        return new String[]{FILE_EXTENSION_1, FILE_EXTENSION_2};
    }

    @Override
    public String getFileDescription() {
        return "Excel data files";
    }

    private class XlsReader {

        protected boolean m_batchMode = true;
        /**
         * the actual data
         */
        protected Instances m_Data;
        /**
         * The document's sheet which consists of all data
         */
        Sheet m_DataSheet;
        Row currentRow;
        Cell currentCell;
        StringTokenizer m_Tokenizer;
        //ключевые номера столбцов аттрибутов:
        ArrayList<Integer> attributeColumnNums = new ArrayList<Integer>();
        //номер столбца весов:
        int weightColumnNum = -1;
        //Флаг для .xlsx файлов:
        boolean m_IsXlsx;
        //Какой-то итератор ~_~
        int currRowNum = 1;
        //

        public XlsReader(FileInputStream fstream, boolean isXlsx) throws IOException {
            this(fstream, 9999, true, isXlsx);
        }

        public XlsReader(FileInputStream fstream, int capacity, boolean isXlsx) throws IOException {
            this(fstream, capacity, true, isXlsx);
        }

        public XlsReader(FileInputStream fstream, int capacity, boolean batch, boolean isXlsx) throws IOException {
            m_batchMode = batch;
            m_IsXlsx = isXlsx;
            if (capacity < 0) {
                throw new IllegalArgumentException("Capacity has to be positive!");
            }
            initDataSheet(fstream);
            readHeader(capacity);
            compactify();
        }

        public void initDataSheet(FileInputStream fstream) throws IOException {
            Workbook wb = (m_IsXlsx) ? new XSSFWorkbook(fstream) : new HSSFWorkbook(fstream);
            m_DataSheet = wb.getSheetAt(0);
        }

        public Instances getStructure() {
            return new Instances(m_Data, 0);
        }

        public Instances getData() {
            return m_Data;
        }

        /**
         * Reads and stores header of an XLS file.
         *
         * @param capacity the number of instances to reserve in the data
         * structure
         * @throws IOException if the information is not read successfully
         */
        public void readHeader(int capacity) throws IOException {
            //Имя отношения:
            String relName;
            //список аттрибутов:
            ArrayList<Attribute> attrs = new ArrayList<Attribute>();
            //разделители для номинальных аттрибутов:
            String delims = " {},";
            Integer relationNameNum = null;
            //Чтение шапок:
            int i = 0;
            currentRow = m_DataSheet.getRow(0);
            currentCell = currentRow.getCell(i);
            int nameCol = -1;
            while ((currentCell != null) && (currentCell.getCellType() != Cell.CELL_TYPE_BLANK)) {
                String cellString = currentCell.getStringCellValue().toLowerCase().trim();
                m_Tokenizer = new StringTokenizer(cellString);
                String token = m_Tokenizer.nextToken();
                switch (token) {
                    case "relname":
                    case "relationname":
                        relationNameNum = i;
                        break;
                    case "attribute":
                    case "attr":
                        Attribute oneAttribute = null;
                        attributeColumnNums.add(i);
                        token = m_Tokenizer.nextToken();
                        switch (token) {
                            case "numeric":
                            case "num": {
                                String attributeName = m_Tokenizer.nextToken();
                                oneAttribute = new Attribute(attributeName);
                                break;
                            }
                            case "nominal":
                            case "nom": {
                                String attributeName = m_Tokenizer.nextToken();
                                ArrayList<String> nominalValues = new ArrayList<String>();
                                while (m_Tokenizer.hasMoreTokens()) {
                                    token = m_Tokenizer.nextToken(delims);
                                    nominalValues.add(token);
                                }
                                oneAttribute = new Attribute(attributeName, nominalValues);
                                break;
                            }
                            default:
                                throw new IllegalArgumentException("Unknown token: \"" + token + "\" from string \"" + cellString + "\" from cell " + currentCell.toString());
                        }
                        attrs.add(oneAttribute);
                        break;
                    case "weight":
                        weightColumnNum = i;
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown token: \"" + token + "\" from string \"" + cellString + "\" from cell " + currentCell.toString());
                }
                currentCell = currentRow.getCell(++i);
            }
            // Check if any attributes have been declared.
            if (attrs.isEmpty()) {
                errorMessage("no attributes declared");
            }
            //Определение имени отношения:
            relName = (relationNameNum == null) ? "DefaultRelationName" : m_DataSheet.getRow(1).getCell(relationNameNum).getStringCellValue();
            //инициализация шапок:
            m_Data = new Instances(relName, attrs, capacity);
        }

        public Instance getInstance(Instances structure) throws IOException {
            m_Data = structure;
            currentRow = m_DataSheet.getRow(currRowNum++);
            // Check if any attributes have been declared.
            if (m_Data.numAttributes() == 0) {
                errorMessage("no header information available");
            }
            // Check if end of file reached.
            if (currentRow == null) {
                return null;
            }
            currentCell = currentRow.getCell(attributeColumnNums.get(0));
            if (currentCell == null || currentCell.getCellType() == Cell.CELL_TYPE_BLANK) {
                return null;
            }
            // Parse instance
            DenseInstance res = new DenseInstance(attributeColumnNums.size());
            int j = 0;
            for (Integer currAttrCol : attributeColumnNums) {
                currentCell = currentRow.getCell(currAttrCol);
                switch (structure.attribute(j).type()) {
                    case Attribute.NUMERIC:
                        res.setValue(j, currentCell.getNumericCellValue());
                        break;
                    case Attribute.NOMINAL:
                        res.setDataset(structure);
                        res.setValue(j, currentCell.getStringCellValue());
                        break;
                    default:
                        throw new IllegalArgumentException("Attribute type \"" + structure.attribute(j).type() + "\" is not supported yet");
                }
                j++;
            }
            if (weightColumnNum > -1) {
                res.setWeight(currentRow.getCell(weightColumnNum).getNumericCellValue());
            }
            return res;
        }

        protected void errorMessage(String msg) throws IOException {
            throw new IOException(msg);
        }

        protected void compactify() {
            m_Data.compactify();
        }
    }
}
