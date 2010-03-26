cd trunk

if [ "x$M2_HOME" == "x" ]; then
	echo variable M2_HOME must be set
	exit 1
fi

ant -file prepare-dep-repos.xml

if [ "x$proxyHost" != "x" ]; then
	proxyOpts="-DproxySet=true -Dhttp.proxyHost=$proxyHost -Dhttp.proxyPort=$proxyPort"
	if [ "x$nonProxyHosts" != "x" ] ; then
		proxyOpts="$proxyOpts -Dhttp.nonProxyHosts=$nonProxyHosts"
	fi
fi

MVN_CALL="$M2_HOME/bin/mvn.bat -Dmaven.repo.local=../.repository $proxyOpts -fae -s settings.xml clean install findbugs:findbugs"
echo $MVN_CALL
eval $MVN_CALL

set result=$?

ant -file prepare-dep-repos.xml cleanup

exit $result
