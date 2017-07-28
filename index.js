import { NativeModules,Component } from 'react-native';

const GPrinter2 = NativeModules.GPrinter2;

const base64Image = "iVBORw0KGgoAAAANSUhEUgAAADAAAAAwCAMAAABg3Am1AAAABGdBTUEAALGPC/xhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAAA8FBMVEUAAABCQkJDQ0NFRUU/Pz9BQUFAQEBERERDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0MAAAA0ZZMIAAAATnRSTlMAAAAAAAAAABWFz8JdBQFHt9OYIxSi/PBsBFHjvCSk/vJt5b7mo26h75ziIZkD1csRXvpziwvx+QadveRSSA3XF6r31DMPOSLWzMTZFgd4wftfAAAAAWJLR0QAiAUdSAAAAAlwSFlzAAALEgAACxIB0t1+/AAAAaBJREFUSMe11dlSwjAUgOE2WmUTQRBtBQVBREREQEVUFkHcz/s/jklbQ7YOhwtz2fzftJ1OTi0rWDaJxRPJ1A6xxEXSu5nsXo7Ylrpskt8vABwcuqIgG94RABRLmtgk+eMTugXliiAI8U7ZRaiqwvnrJUH7WnBRFfR5zsKeinoohN4XRHyeZc8F2RJ6SSh9KJReeCpH7QOh9st76L3/5lrPRf5c6wEaF039IlQvmYgXAL1aVxQk8D20YxQk1wDXHQpuGui+22Pv4FbK2L5/639Rt44TYY8WvEcKoUcJqUcIpV8ptN4Xd5H9vd5TMXiIBMOOoXe8x0igzJKgf6pB9JJmCaIXJkPYb6/oFYHoJYHqxXllo/qlcDxcz8VzE9lTkWInLoPuAZIjCrJrgPGEgtYaYDqgIFc07LwMTbNkNmfvQEpVbafbfzXMkvbCn622Lth50adP2BuEf740MVvwP4oi+LyShNArQphXgpB69v/jQppXXCi9IJR5FQqt50KbV74w9Ey8td4/etq8Sn1+TeeGngn3u5PW7myPJj/G/v/WL4DMswebZ4AxAAAAJXRFWHRkYXRlOmNyZWF0ZQAyMDE1LTA2LTI1VDA4OjQ0OjQ2KzA4OjAww1b9dwAAACV0RVh0ZGF0ZTptb2RpZnkAMjAxNS0wNi0yNVQwODo0NDo0NiswODowMLILRcsAAAAASUVORK5CYII=";

const DIRECTION = {
    FORWARD: 0,
    BACKWARD: 1
};

const DENSITY = {
    DNESITY0: 0,
    DNESITY1: 1,
    DNESITY2: 2,
    DNESITY3: 3,
    DNESITY4: 4,
    DNESITY5: 5,
    DNESITY6: 6,
    DNESITY7: 7,
    DNESITY8: 8,
    DNESITY9: 9,
    DNESITY10: 10,
    DNESITY11: 11,
    DNESITY12: 12,
    DNESITY13: 13,
    DNESITY14: 14,
    DNESITY15: 15
};
const BARCODETYPE = {
    CODE128: "128",
    CODE128M: "128M",
    EAN128: "EAN128",
    ITF25: "25",
    ITF25C: "25C",
    CODE39: "39",
    CODE39C: "39C",
    CODE39S: "39S",
    CODE93: "93",
    EAN13: "EAN13",
    EAN13_2: "EAN13+2",
    EAN13_5: "EAN13+5",
    EAN8: "EAN8",
    EAN8_2: "EAN8+2",
    EAN8_5: "EAN8+5",
    CODABAR: "CODA",
    POST: "POST",
    UPCA: "EAN13",
    UPCA_2: "EAN13+2",
    UPCA_5: "EAN13+5",
    UPCE: "EAN13",
    UPCE_2: "EAN13+2",
    UPCE_5: "EAN13+5",
    CPOST: "CPOST",
    MSI: "MSI",
    MSIC: "MSIC",
    PLESSEY: "PLESSEY",
    ITF14: "ITF14",
    EAN14: "EAN14"
};
const FONTTYPE = {
    FONT_1: "1",
    FONT_2: "2",
    FONT_3: "3",
    FONT_4: "4",
    FONT_5: "5",
    FONT_6: "6",
    FONT_7: "7",
    FONT_8: "8",
    SIMPLIFIED_CHINESE: "TSS24.BF2",
    TRADITIONAL_CHINESE: "TST24.BF2",
    KOREAN: "K"
};
const EEC = {
    LEVEL_L: "L",
    LEVEL_M: "M",
    LEVEL_Q: "Q",
    LEVEL_H: "H"

};
const ROTATION = {
    ROTATION_0: 0,
    ROTATION_90: 90,
    ROTATION_180: 180,
    ROTATION_270: 270
};
const FONTMUL = {
    MUL_1: 1,
    MUL_2: 2,
    MUL_3: 3,
    MUL_4: 4,
    MUL_5: 5,
    MUL_6: 6,
    MUL_7: 7,
    MUL_8: 8,
    MUL_9: 9,
    MUL_10: 10
};
const MIRROR = {
    NORMAL: 0,
    MIRROR: 1
};
const BITMAP_MODE = {
    OVERWRITE: 0,
    OR: 1,
    XOR: 2
};
const PRINT_SPEED = {
    SPEED1DIV5:1,
    SPEED2:2,
    SPEED3:3,
    SPEED4:4
};
const SAMPLE_LABEL_OPTIONS = {
    width: 30,
    height: 50,
    gap: 30,
    direction: DIRECTION.FORWARD,
    density:DENSITY.DNESITY3,
    mirror: MIRROR.NORMAL,
    speed:PRINT_SPEED.SPEED1DIV5,
    reference: [0, 0],
    tear: 0,
    sound: 0,
    address: "DC:0D:30:04:33:69",
    reverse:[{x:0,y:0,width:0,height:0}],
    text: [{
        text: 'I am a testing txt',
        x: 20,
        y: 0,
        fonttype: FONTTYPE.SIMPLIFIED_CHINESE,
        rotation: ROTATION.ROTATION_0,
        xscal:FONTMUL.MUL_1,
        yscal: FONTMUL.MUL_1
    },{
        text: '你在说什么呢?',
        x: 20,
        y: 100,
        fonttype: FONTTYPE.SIMPLIFIED_CHINESE,
        rotation: ROTATION.ROTATION_0,
        xscal:FONTMUL.MUL_1,
        yscal: FONTMUL.MUL_1,
        bold:true
    }],
    qrcode: [{x: 300, y: 30, level: EEC.LEVEL_L, width: 3, rotation: ROTATION.ROTATION_0, code: 'show me the money'}],
    barcode: [{x: 160, y:150, type: BARCODETYPE.CODE128, height: 40, readabel: 1, rotation: ROTATION.ROTATION_0, code: '1234567890'}],
    image: [{x: 0, y: 0, mode: BITMAP_MODE.OVERWRITE, width: 200, image: base64Image}]
};

module.exports = {
    printTestPage(address){
        return GPrinter2.printTestPage(address);
    },
    printLabel(options){
        return GPrinter2.printLabel(options);
    },
    calibration(address){
        return GPrinter2.calibration(address);
    },
    setCutOn(address){
        return GPrinter2.setCutOn(address);
    },
    setCutOff(address){
        return GPrinter2.setCutOff(address);
    },
    printReceipt(options){
        return GPrinter2.printReceipt(options);
    },
    BITMAP_MODE:BITMAP_MODE,
    DENSITY:DENSITY,
    BARCODETYPE:BARCODETYPE,
    FONTTYPE:FONTTYPE,
    EEC:EEC,
    ROTATION:ROTATION,
    MIRROR:MIRROR,
    DIRECTION:DIRECTION,
    FONTMUL:FONTMUL,
    PRINT_SPEED:PRINT_SPEED
}