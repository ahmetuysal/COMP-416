# Internet Protocol Stack

Internet protocol stack consist of five layers:

1. **Application Layer**: Contains the communications protocols and interface methods used in **process-to-process** communications across an Internet Protocol (IP) computer network. The application layer only standardizes communication and depends upon the underlying transport layer protocols to establish host-to-host data transfer channels and manage the data exchange in a client-server or peer-to-peer networking model. FTP, Telnet, SMTP, DNS, and HTTP are some examples protocols from the application layer.
2. **Transport Layer**: Provides **process-to-process** communication services for applications. TCP (Transmission Control Protocol) and UDP (User Datagram Protocal) are protocols from transport layer.
3. **Network Layer**: Responsible for packet forwarding including routing through intermediate routers. IPv4/IPv6, and IPSec are protocols from network layer.
4. **Link Layer**: Transfers data between adjacent network nodes in a wide area network (WAN) or between nodes on the same local area network (LAN) segment. Ethernet and Wifi are examples of link layer protocols.
5. **Physical Layer**: Consists of the electronic circuit transmission technologies of a network. Transmits raw bits.

Layers use encapsulation to frame data from upper layers. Packets are named differently in each layer:

1. Application Layer: _message_
2. Transport Layer: _segment_
3. Network Layer: _datagram_
4. Link Layer: _frame_

![Sample encapsulation of application data from UDP to a Link protocol frame
](images/encapsulation.png)

# Loss and Delay

Packets are queued in router buffers if packet arrival rate to link (temporarily) exceeds output link capacity. This causes _queueing delay_, packets are dropped if there is no available buffer space and this causes _packet loss_.

_d_<sub>_nodal_</sub> = _d_<sub>_processing_</sub> + _d_<sub>_queueing_</sub> + _d_<sub>_transmission_</sub> + _d_<sub>_propagation_</sub>

## _d_<sub>_nodal_</sub>

- check bit errors
- determine output link
- typically < msec

## _d_<sub>_queue_</sub>

- time waiting at output link for transmission
- depends on congestion level of router

## _d_<sub>_trans_</sub>

- packet length (L) / link bandwidth (R)

## _d_<sub>_prop_</sub>

- length of the physical link (_d_) / propagation speed (_s_)
- _s_ ~2x10<sup>8</sup> m/s

## Traffic Intensity = L (packet length) \* a (average packet arrival rate) / R (link bandwidth)

- La/R ~ 0: average _d_<sub>_queue_</sub> small
- La/R -> 1: average _d_<sub>_queue_</sub> large
- La/R > 1: average _d_<sub>_queue_</sub> infinite (more works is arriving than service capacity)

# Throughput

Rate (bits/time unit) at which bits transferred between sender/receiver. Can be calculated as intantaneous or average over a longer period of time.

End-to-end throughput of a path is equal to the throughput of the minimum capacity link. This is called _bottleneck_ link.
