const checker = require('license-checker');
const fs = require('fs');
const path = require("path");
const printf = require('printf');
const xmlbuilder2 = require('xmlbuilder2');

const outputFile = 'dist/licenses.xml';

function createLicenseBundle() {
    let done;
    if(this.async) {
        done = this.async();
    } else {
        done = () => {};
    }
    checker.init({
        start: './',
        production: true,
        customFormat: {
            version: '',
            description: '',
            repository: '',
            publisher: '',
            licenses: '',
            licenseFile: '',
            licenseText: '',
            licenseModified: '',
            noticeFile: '',
            author: '',
            homepage: ''
        }
    }, function (err, packages) {
        if (err) {
            done(err);
        } else {
            console.log("Exporting license data to: " + outputFile);
            const document = xmlbuilder2.create({version: '1.0'});
            const root = document.ele('packages');
            for (const key in packages) {
                const package = packages[key];
                const versionSplit = key.lastIndexOf("@");
                const groupSplit = key.indexOf("/");
                let group;
                if (groupSplit >= 0) {
                    group = key.substring(0, groupSplit);
                } else {
                    group = "";
                }
                const artifact = key.substring(groupSplit >= 0 ? (groupSplit + 1) : 0, versionSplit);
                const version = key.substring(versionSplit + 1);
                const packageRoot = root.ele("package");
                packageRoot.ele("group").txt(escapeAmpersand(group));
                packageRoot.ele("artifact").txt(escapeAmpersand(artifact));
                packageRoot.ele("version").txt(escapeAmpersand(version));
                if (package["description"]) {
                    packageRoot.ele("description").txt(escapeAmpersand(package["description"]));
                }
                packageRoot.ele("fileLocation").txt(escapeAmpersand(package["path"]));
                if (package["repository"]) {
                    packageRoot.ele("repository").txt(escapeAmpersand(package["repository"]));
                }
                const licenses = (typeof package['licenses'] === "object" && "length" in package['licenses']) ? package['licenses'] : [package['licenses']];
                const licensesNode = packageRoot.ele("licenses");
                for (const license of licenses) {
                    const licenseNode = licensesNode.ele("license");
                    licenseNode.ele("name").txt(escapeAmpersand(license));
                }
                if (package["licenseText"]) {
                    packageRoot.ele("artifactLicense").txt(escapeAmpersand(package["licenseText"].replace(/\r\n/g, "\n")));
                }
                if (package["noticeFile"]) {
                    const content = fs.readFileSync(package["noticeFile"], {encoding: 'utf8'});
                    packageRoot.ele("artifactNotice").txt(escapeAmpersand(content.replace(/\r\n/g, "\n")));
                }
                if (package['homepage']) {
                    packageRoot.ele("url").txt(escapeAmpersand(package['homepage']));
                }
            }
            const packagesData = root.end({prettyPrint: true});
            fs.writeFileSync(outputFile, packagesData, {encoding: 'utf8'});
            done(true);
        }
    });
}


function escapeAmpersand(input) {
    // Bug in xmlbuilder2 - ampersands are not escaped
    return input.replace(/&/g, "&amp;");
}

if (require.main === module) {
    createLicenseBundle();
} else {
    module.exports = grunt  => {
        grunt.registerTask( 'bundle-licenses', 'Create summary file for licenses',  createLicenseBundle);
    };
}
