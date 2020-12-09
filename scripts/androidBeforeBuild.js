const fs = require('fs');
const parseString = require('xml2js').parseString;

const rootDir = 'platforms/android/app/src/main';

const main = () => {
	fs.readFile(`${rootDir}/AndroidManifest.xml`, 'utf-8', (error, data) => {
		if (error) return console.log(error);

		getBundleDir(data, (err, bundlePath) => {
			if (err) return console.log(err);

			modifyMainActivity(bundlePath);
		});
	});
};

/**
 * @function getBundleDir
 * takes in utf-8 encoded AndroidManifest.xml
 * and returns the bundle path
 * @example output: com/any/appf01234b
 * @param {String} xml 
 * @param {Function} callback 
 */
const getBundleDir = (xml, callback) => {
	parseString(xml, {}, (error, data) => {
		if (error) return console.log(error);
	
		if (!data || !data.manifest || !data.manifest.$ || !data.manifest.$.package) {
			return console.log('no package found', null);
		}

		let bundle = data.manifest.$.package;

		callback(null, bundle.split('.').join('/'));
	});
};

const modifyMainActivity = (bundlePath) => {
	const file = `${rootDir}/java/${bundlePath}/MainActivity.java`;

	fs.readFile(file, 'utf-8', (err, data) => {
		if (err) return console.log(err);

		data = data.replace('import org.apache.cordova.*;', 'import com.ibeaconbg.www.MainCordovaActivity;\nimport org.apache.cordova.CordovaActivity;');

		data = data.replace('extends CordovaActivity', 'extends MainCordovaActivity');

		data = data.substring(0, data.indexOf('{')) + '{}';

		data = data + `\nclass fauxCordovaActivity extends CordovaActivity {}`;

		fs.writeFile(file, data, 'utf-8', console.log);
	});
};

main();