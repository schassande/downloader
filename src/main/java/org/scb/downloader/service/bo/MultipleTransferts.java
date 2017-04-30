package org.scb.downloader.service.bo;

import java.util.List;

import org.scb.downloader.domain.DWTransfert;

public class MultipleTransferts {

	private List<DWTransfert> transferts;

	public List<DWTransfert> getTransferts() {
		return transferts;
	}

	public void setTransferts(List<DWTransfert> transferts) {
		this.transferts = transferts;
	}

	@Override
	public String toString() {
		return "MultipleTransferts [transfert=" + transferts + "]";
	}
}
