const checker = require('license-checker');
const fs = require('fs');
const path = require("path");
const printf = require('printf');
const xmlbuilder2 = require('xmlbuilder2');

const outputFile = 'dist/licenses.xml';

class BundleLicenses {
    apply(compiler) {
        compiler.hooks.done.tap('BundleLicenses', (compilation) => {
            console.log("Hooking");
            this.execute();
        });
    }

    execute() {
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
            console.log("Exporting license data to: " + outputFile);
            const document = xmlbuilder2.create({
                version: '1.0'
            });
            const root = document.ele('packages');
            for (const key in packages) {
                const package_ = packages[key];
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
                packageRoot.ele("scope", "compile");
                packageRoot.ele("group").txt(BundleLicenses.escapeAmpersand(group));
                packageRoot.ele("artifact").txt(BundleLicenses.escapeAmpersand(artifact));
                packageRoot.ele("version").txt(BundleLicenses.escapeAmpersand(version));
                if (package_["description"]) {
                    packageRoot.ele("description").txt(BundleLicenses.escapeAmpersand(package_["description"]));
                }
                packageRoot.ele("fileLocation").txt(BundleLicenses.escapeAmpersand(package_["path"]));
                if (package_["repository"]) {
                    packageRoot.ele("repository").txt(BundleLicenses.escapeAmpersand(package_["repository"]));
                }
                const licenses = (typeof package_['licenses'] === "object" && "length" in package_['licenses']) ? package_['licenses'] : [package_['licenses']];
                const licensesNode = packageRoot.ele("licenses");
                for (const license of licenses) {
                    const licenseNode = licensesNode.ele("license");
                    licenseNode.ele("name").txt(BundleLicenses.escapeAmpersand(license));
                }
                if (package_["licenseText"]) {
                    packageRoot.ele("artifactLicense").txt(BundleLicenses.escapeAmpersand(package_["licenseText"].replace(/\r\n/g, "\n")));
                }
                if (package_["noticeFile"]) {
                    const content = fs.readFileSync(package_["noticeFile"], {
                        encoding: 'utf8'
                    });
                    packageRoot.ele("artifactNotice").txt(BundleLicenses.escapeAmpersand(content.replace(/\r\n/g, "\n")));
                }
                ;
                if (package_['homepage']) {
                    packageRoot.ele("url").txt(BundleLicenses.escapeAmpersand(package_['homepage']));
                }
            }
            const packagesData = root.end({
                prettyPrint: true
            });
            fs.writeFileSync(outputFile, packagesData, {
                encoding: 'utf8'
            });
        });
    }

    static escapeAmpersand(input) {
        // Bug in xmlbuilder2 - ampersands are not escaped
        return input.replace(/&/g, "&amp;");
    }
}

module.exports = BundleLicenses;
