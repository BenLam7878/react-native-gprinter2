/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 * @flow
 */

import React, { Component } from 'react';
import {
    AppRegistry,
    StyleSheet,
    Text,
    View,
    Button
} from 'react-native';
import GPrinter from 'react-native-gprinter2'

export default class samples extends Component {
    constructor(props) {
        super(props);
        this.state = {
            cut: false
        }
    }

    async _print() {
        try {
            console.log("click")
            let error = await GPrinter.printTestPage("DC:0D:30:04:33:69");
            //if(error){
            console.log(error);
            //}
        } catch (e) {
            console.log("err");
            console.log(e);
        }
    }

    async _setCut() {
        if (this.state.cut) {
            let error = await GPrinter.setCutOff("DC:0D:30:04:33:69");
            console.log(error);
            if (!error) {
                this.setState({
                    cut: false
                })
            }
        } else {
            let error = await GPrinter.setCutOn("DC:0D:30:04:33:69");
            console.log(error);
            if (!error) {
                this.setState({
                    cut: true
                })
            }
        }
    }

    async _printLable() {
        try {
            var options = {
                width: 30,
                height: 50,
                gap: 30,
                direction: GPrinter.DIRECTION.FORWARD,
                mirror: GPrinter.MIRROR.NORMAL,
                reference: [0, 0],
                tear: 0,
                sound: 0,
                address: "DC:0D:30:04:33:69",
                text: [{
                    text: 'I am a testing txt',
                    x: 20,
                    y: 0,
                    fonttype: GPrinter.FONTTYPE.SIMPLIFIED_CHINESE,
                    rotation: GPrinter.ROTATION.ROTATION_0,
                    xscal:GPrinter.FONTMUL.MUL_1,
                    yscal: GPrinter.FONTMUL.MUL_1
                },{
                    text: '你在说什么呢?',
                    x: 20,
                    y: 100,
                    fonttype: GPrinter.FONTTYPE.SIMPLIFIED_CHINESE,
                    rotation: GPrinter.ROTATION.ROTATION_0,
                    xscal:GPrinter.FONTMUL.MUL_1,
                    yscal: GPrinter.FONTMUL.MUL_1
                }],
                qrcode: [{x: 300, y: 30, level: GPrinter.EEC.LEVEL_L, width: 3, rotation: GPrinter.ROTATION.ROTATION_0, code: 'show me the money'}],
                barcode: [{x: 160, y:150, type: GPrinter.BARCODETYPE.CODE128, height: 40, readabel: 1, rotation: GPrinter.ROTATION.ROTATION_0, code: '1234567890'}]
            };
            let error = await GPrinter.printLabel(options);
            console.log(error);
        } catch (err) {
            console.log("err");
            console.log(err);
        }
    }

    render() {
        return (
            <View style={styles.container}>
                <Button title="打印测试页" onPress={this._print.bind(this)}></Button>

                <Button title="打印标签" onPress={this._printLable.bind(this)}></Button>
                <Button title={"设置切纸("+(this.state.cut?'开':'关')+")"} onPress={this._setCut.bind(this)}></Button>
                <Text style={styles.instructions}>
                    To get started, edit index.android.js
                </Text>
                <Text style={styles.instructions}>
                    Double tap R on your keyboard to reload,{'\n'}
                    Shake or press menu button for dev menu
                </Text>
            </View>
        );
    }
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: '#F5FCFF',
    },
    welcome: {
        fontSize: 20,
        textAlign: 'center',
        margin: 10,
    },
    instructions: {
        textAlign: 'center',
        color: '#333333',
        marginBottom: 5,
    },
});

AppRegistry.registerComponent('samples', () => samples);
