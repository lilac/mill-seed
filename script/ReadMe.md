## Introduction

This folder contains ammonite scripts that I wrote. Note that these scripts depends on some libraries that only work on
Scala 2.12, thus a variant of Ammonite with Scala 2.12 is provided under the folder.

- **K8s client**

  The [k8s](./k8s.sc) script is an example of accessing k8s api server in Java/Scala.

- **Stress test of tidb using Gatling**

  The [Stress Test](./StressTest.sc) script tries to simulate a simple kv usage pattern, and see the performance of tidb
  and tikv. The expectation is read latency of under 10ms, but that is not met. A rough test show the latency to largely
  falls in 10 to 50 milliseconds.

  The tidb cluster was set up following
  this [guide](https://docs.pingcap.com/tidb-in-kubernetes/stable/get-started#deploy-tidb-operator).